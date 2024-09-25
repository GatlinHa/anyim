package com.hibob.anyim.netty.server.processor;

import com.alibaba.nacos.api.exception.NacosException;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.netty.config.NacosConfig;
import com.hibob.anyim.netty.mq.kafka.KafkaProducer;
import com.hibob.anyim.netty.protobuf.Body;
import com.hibob.anyim.netty.protobuf.Header;
import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.protobuf.MsgType;
import com.hibob.anyim.netty.utils.SpringContextUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.*;

import static com.hibob.anyim.common.constants.Const.SPLIT_V;
import static com.hibob.anyim.common.utils.CommonUtil.combineId;
import static com.hibob.anyim.common.utils.CommonUtil.sortId;
import static com.hibob.anyim.netty.server.ws.WebSocketServer.getLocalRoute;

public abstract class MsgProcessor {
    @Value("${custom.ref-msg-id.default:10000}")
    private int refMsgIdDefault;

    @Value("${custom.ref-msg-id.step:10000}")
    private int refMsgIdStep;

    @Autowired
    private NacosConfig nacosConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RpcClient rpcClient;


    public abstract void process(ChannelHandlerContext ctx, Msg msg) throws Exception;

    long computeMsgId(ChannelHandlerContext ctx, String sessionId) {
        String msgIdKey = RedisKey.NETTY_REF_MSG_ID + sessionId;
        long refMsgId;
        AttributeKey<Object> attributeKey = AttributeKey.valueOf(msgIdKey);
        Object object = ctx.channel().attr(attributeKey).get();
        if (object == null) {
            refMsgId = rpcClient.getChatRpcService().refMsgId(sessionId, refMsgIdDefault);
            ctx.channel().attr(attributeKey).set(refMsgId); // 将refMsgId保存在channel中，不用每次都查数据库
        }
        else {
            refMsgId = (long) object;
        }

        Long msgId = redisTemplate.opsForValue().increment(msgIdKey);
        if (msgId < refMsgIdDefault) { //应对第一条消息，redis重启等场景
            msgId = redisTemplate.opsForValue().increment(msgIdKey, refMsgId);
            refMsgId = rpcClient.getChatRpcService().updateAndGetRefMsgId(sessionId, refMsgIdStep, refMsgId);
            ctx.channel().attr(attributeKey).set(refMsgId);
        }
        else if (refMsgId - msgId < refMsgIdStep / 2) { //msgId自增到一定程度，refMsgId需要更新
            refMsgId = rpcClient.getChatRpcService().updateAndGetRefMsgId(sessionId, refMsgIdStep, refMsgId);
            ctx.channel().attr(attributeKey).set(refMsgId);
        }

        return msgId;
    }

    void saveMsg(Msg msg, long msgId) {
        String sessionId = null;
        String remoteId = null;
        if (msg.getHeader().getMsgType() == MsgType.CHAT) {
            String fromId = msg.getBody().getFromId();
            String toId =  msg.getBody().getToId();
            remoteId = toId;
            String[] sorted = sortId(fromId, toId);
            sessionId = combineId(sorted[0], sorted[1]);
        }
        else if (msg.getHeader().getMsgType() == MsgType.GROUP_CHAT) {
            remoteId = String.valueOf(msg.getBody().getGroupId());
            sessionId = remoteId;
        }

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("sessionId", sessionId);
        msgMap.put("fromId", msg.getBody().getFromId());
        msgMap.put("fromClient", msg.getBody().getFromClient());
        msgMap.put("remoteId", remoteId);
        msgMap.put("msgId", msgId);
        msgMap.put("msgType", msg.getHeader().getMsgType().getNumber());
        msgMap.put("content", msg.getBody().getContent());
        msgMap.put("msgTime", new Date());
        //TODO RPC的能力有限，后面还是要考虑换成MQ
        rpcClient.getChatRpcService().asyncSaveMsg(msgMap);
    }

    void sendDeliveredMsg(ChannelHandlerContext ctx, Msg msg, String sessionId, Long msgId) {
        Header readMsgheader = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0)
                .setMsgType(MsgType.DELIVERED)
                .setIsExtension(false)
                .build();
        Body readMsgBody = Body.newBuilder()
                .setSessionId(sessionId)
                .setTempMsgId(msg.getBody().getTempMsgId())
                .setMsgId(msgId)
                .build();
        Msg readMsg = Msg.newBuilder().setHeader(readMsgheader).setBody(readMsgBody).build();
        ctx.writeAndFlush(readMsg);
    }

    Set<Object> queryOnlineClient(String account) {
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

    /**
     *  扩散给自己的其他客户端
     * @param msg
     * @throws NacosException
     */
    void syncOtherClients(Msg msg, String sessionId, Long msgId) throws NacosException {
        String fromId = msg.getBody().getFromId();
        String fromClient = msg.getBody().getFromClient();
        Set<Object> fromOnlineClients = queryOnlineClient(fromId);
        fromOnlineClients.remove(CommonUtil.conUniqueId(fromId, fromClient)); //移除发消息这个client
        for (Object fromUniqueId : fromOnlineClients) {
            Msg msgOut = Msg.newBuilder(msg)
                    .setHeader(msg.getHeader().toBuilder()
                            .setMsgType(MsgType.SENDER_SYNC).build())
                    .setBody(msg.getBody().toBuilder() //修改发给本账号其它client的toId和toClient
                            .setToId(fromId)
                            .setToClient(((String) fromUniqueId).split(SPLIT_V)[1])
                            .setSessionId(sessionId)
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
    void sendToClients(Msg msg, String toId, Long msgId) throws NacosException {
        Set<Object> toOnlineClients = queryOnlineClient(toId);
        for (Object toUniqueId : toOnlineClients) {
            Msg msgOut = Msg.newBuilder(msg).setBody(msg.getBody().toBuilder()
                    .setToId(toId)
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
    void sendToMembers(Msg msg, List<String> members, Long msgId) throws NacosException {
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
}
