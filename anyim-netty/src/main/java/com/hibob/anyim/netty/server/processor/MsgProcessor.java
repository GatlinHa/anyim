package com.hibob.anyim.netty.server.processor;

import com.alibaba.nacos.api.exception.NacosException;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.netty.config.NacosConfig;
import com.hibob.anyim.netty.mq.kafka.KafkaProducer;
import com.hibob.anyim.common.protobuf.Body;
import com.hibob.anyim.common.protobuf.Header;
import com.hibob.anyim.common.protobuf.Msg;
import com.hibob.anyim.common.protobuf.MsgType;
import com.hibob.anyim.netty.utils.SpringContextUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.*;

import static com.hibob.anyim.common.constants.Const.SPLIT_C;
import static com.hibob.anyim.common.constants.Const.SPLIT_V;
import static com.hibob.anyim.netty.server.ws.WebSocketServer.getLocalRoute;

public abstract class MsgProcessor {

    @Autowired
    private NacosConfig nacosConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RpcClient rpcClient;


    public abstract void process(ChannelHandlerContext ctx, Msg msg) throws Exception;

    /**
     * 用于msg重复性校验，按照客户端填写的seq校验，如果重复则返回对应的msgId，否则返回null
     * @param msg
     * @return
     */
    protected Long getSeqMsgIdCache(Msg msg) {
        String sessionId = msg.getBody().getSessionId();
        String seq = msg.getBody().getSeq();
        String key = RedisKey.NETTY_SEQ_MSG_ID + sessionId + SPLIT_C + seq;
        Integer value = (Integer)redisTemplate.opsForValue().get(key); //这里redis返回的是Integer
        return value == null ? null : value.longValue();
    }

    /**
     * 用于msg重复性校验的seq缓存，过期时间为1800s
     * @param msg
     * @param msgId
     */
    protected void setSeqMsgIdCache(Msg msg, Long msgId) {
        String sessionId = msg.getBody().getSessionId();
        String seq = msg.getBody().getSeq();
        String key = RedisKey.NETTY_SEQ_MSG_ID + sessionId + SPLIT_C + seq;
        redisTemplate.opsForValue().set(key, msgId, Duration.ofSeconds(Const.SEQ_EXPIRE));
    }

    /**
     * 消息入库，当前采用服务方异步入库，因此不支持等待回调结果。
     * @param msg
     * @param msgId
     */
    protected void saveMsg(Msg msg, long msgId) {
        String sessionId = msg.getBody().getSessionId();
        String remoteId = null;
        if (msg.getHeader().getMsgType() == MsgType.CHAT) {
            remoteId = msg.getBody().getToId();
        }
        else if (msg.getHeader().getMsgType() == MsgType.GROUP_CHAT) {
            remoteId = msg.getBody().getGroupId();
        }

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("sessionId", sessionId);
        msgMap.put("fromId", msg.getBody().getFromId());
        msgMap.put("fromClient", msg.getBody().getFromClient());
        msgMap.put("remoteId", remoteId);
        msgMap.put("msgId", msgId);
        msgMap.put("msgType", msg.getHeader().getMsgType().getNumber());
        msgMap.put("content", CommonUtil.truncate(msg.getBody().getContent(), Const.MSG_CONTENT_LIMIT));
        msgMap.put("msgTime", new Date());
        //TODO RPC的能力有限，后面还是要考虑换成MQ
        rpcClient.getChatRpcService().asyncSaveMsg(msgMap);
    }

    /**
     * 回复已送达
     * @param ctx
     * @param msg
     * @param msgId
     */
    protected void sendDeliveredMsg(ChannelHandlerContext ctx, Msg msg, Long msgId) {
        Header header = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0)
                .setMsgType(MsgType.DELIVERED)
                .setIsExtension(false)
                .build();
        Body body = Body.newBuilder()
                .setSessionId(msg.getBody().getSessionId())
                .setSeq(msg.getBody().getSeq())
                .setMsgId(msgId)
                .build();
        Msg deliveredMsg = Msg.newBuilder().setHeader(header).setBody(body).build();
        ctx.writeAndFlush(deliveredMsg);
    }

    /**
     *  扩散给自己的其他客户端
     * @param msg
     * @throws NacosException
     */
    protected void syncOtherClients(Msg msg, Long msgId) throws NacosException {
        String fromId = msg.getBody().getFromId();
        String fromClient = msg.getBody().getFromClient();
        Set<Object> fromOnlineClients = queryOnlineClient(fromId);
        fromOnlineClients.remove(CommonUtil.conUniqueId(fromId, fromClient)); //移除发消息这个client
        for (Object fromUniqueId : fromOnlineClients) {
            Msg msgOut = Msg.newBuilder(msg)
                    .setHeader(msg.getHeader().toBuilder()
                            .build())
                    .setBody(msg.getBody().toBuilder() //修改发给本账号其它client的toId和toClient
                            .setToId(fromId)
                            .setToClient(((String) fromUniqueId).split(SPLIT_V)[1])
                            .setMsgId(msgId)
                            .build())
                    .build();

            String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + fromUniqueId;
            String instance = (String)redisTemplate.opsForValue().get(routeKey);
            if (!nacosConfig.isValidInstance(instance)) continue; // 如果目标实例不在注册中心，则不发送
            if (instance.equals(nacosConfig.getInstance())) { // 如果目标实例就是本机，则找到对应的channel
                getLocalRoute().get(routeKey).writeAndFlush(msgOut);
                continue;
            }
            KafkaProducer producer = SpringContextUtil.getBean(KafkaProducer.class);
            producer.sendChatMessage(instance, msgOut);
        }
    }

    /**
     * 扩散给接收端的（多个）客户端
     * @param msg
     * @param toId
     * @param msgId
     * @throws NacosException
     */
    protected void sendToClients(Msg msg, String toId, Long msgId) throws NacosException {
        Set<Object> toOnlineClients = queryOnlineClient(toId);
        for (Object toUniqueId : toOnlineClients) {
            Msg msgOut = Msg.newBuilder(msg).setBody(msg.getBody().toBuilder()
                    .setToClient(((String) toUniqueId).split(SPLIT_V)[1])
                    .setMsgId(msgId)
                    .build()).build();

            String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + toUniqueId;
            String instance = (String)redisTemplate.opsForValue().get(routeKey);
            if (!nacosConfig.isValidInstance(instance)) continue; // 如果目标实例不在注册中心，则不发送
            if (instance.equals(nacosConfig.getInstance())) { // 如果目标实例就是本机，则找到对应的channel
                getLocalRoute().get(routeKey).writeAndFlush(msgOut);
                continue;
            }
            KafkaProducer producer = SpringContextUtil.getBean(KafkaProducer.class);
            producer.sendChatMessage(instance, msgOut);
        }
    }

    /**
     * 群消息发给群成员
     * @param msg
     * @param members
     * @param msgId
     * @throws NacosException
     */
    protected void sendToMembers(Msg msg, List<String> members, Long msgId) throws NacosException {
        for (String memberAccounts : members) {
            if (memberAccounts.equals(msg.getBody().getFromId())) {
                continue; //移除自己，不给自己发
            }

            // 扩散给每个成员的每个客户端
            Set<Object> toOnlineClients = queryOnlineClient(memberAccounts);
            for (Object toUniqueId : toOnlineClients) {
                Msg msgOut = Msg.newBuilder(msg).setBody(msg.getBody().toBuilder()
                        .setToId(memberAccounts)
                        .setToClient(((String) toUniqueId).split(SPLIT_V)[1])
                        .setMsgId(msgId)
                        .build()).build();

                String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + toUniqueId;
                String instance = (String)redisTemplate.opsForValue().get(routeKey);
                if (!nacosConfig.isValidInstance(instance)) continue; // 如果目标实例不在注册中心，则不发送
                if (instance.equals(nacosConfig.getInstance())) { // 如果目标实例就是本机，则找到对应的channel
                    getLocalRoute().get(routeKey).writeAndFlush(msgOut);
                    continue;
                }
                KafkaProducer producer = SpringContextUtil.getBean(KafkaProducer.class);
                producer.sendChatMessage(instance, msgOut);
            }
        }
    }


    /**
     * 发送已读消息时，入库保存
     * @param fromId
     * @param sessionId
     * @param readMsgId
     */
    protected void updateReadMsgId(String fromId, String sessionId, String readMsgId) {
        Map<String, Object> map = new HashMap<>();
        map.put("account", fromId);
        map.put("sessionId", sessionId);
        map.put("readMsgId", readMsgId);
        map.put("readTime", new Date());
        rpcClient.getChatRpcService().updateReadMsgId(map);
    }


    private Set<Object> queryOnlineClient(String account) {
        String onlineKey = RedisKey.NETTY_ONLINE_CLIENT + account;
        Set<Object> members = redisTemplate.opsForSet().members(onlineKey);
        if (members.size() == 0) { //缓存未命中，调RPC查数据库后同步缓存
            rpcClient.getUserRpcService().queryOnline(account).forEach(x -> {
                redisTemplate.opsForSet().add(onlineKey, x);
                members.add(x);
            });
            redisTemplate.expire(onlineKey, Duration.ofSeconds(Const.CACHE_ONLINE_EXPIRE));
        }
        return members;
    }
}
