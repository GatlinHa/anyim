package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.netty.config.NacosConfig;
import com.hibob.anyim.netty.mq.kafka.KafkaProducer;
import com.hibob.anyim.netty.protobuf.Body;
import com.hibob.anyim.netty.protobuf.Header;
import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.protobuf.MsgType;
import com.hibob.anyim.netty.rpc.RpcClient;
import com.hibob.anyim.netty.utils.SpringContextUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.hibob.anyim.common.constants.Const.SPLIT_V;
import static com.hibob.anyim.common.utils.CommonUtil.combineId;
import static com.hibob.anyim.netty.server.ws.WebSocketServer.getLocalRoute;

@Component
public class ChatProcessor implements MsgProcessor{

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

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        String fromId = msg.getBody().getFromId();
        String fromClient = msg.getBody().getFromClient();
        String toId = msg.getBody().getToId(); //端侧发过来的消息，也不知道要发给哪个client，所以没填toClient
        String sessionId = combineId(fromId, toId);
        String msgIdKey = RedisKey.NETTY_REF_MSG_ID + sessionId;
        Long msgId = redisTemplate.opsForValue().increment(msgIdKey);
        long refMsgId;
        AttributeKey<Object> attributeKey = AttributeKey.valueOf(msgIdKey);
        Object object = ctx.channel().attr(attributeKey).get();
        if (object == null) { // 将refMsgId保存在channel中，不用每次都查数据库
            refMsgId = rpcClient.getChatRpcService().refMsgId(fromId, toId, refMsgIdDefault);
            ctx.channel().attr(attributeKey).set(refMsgId);
        }
        else {
            refMsgId = (long) object;
        }

        if (msgId < refMsgIdDefault) { //应对第一条消息，redis重启等场景
            msgId = redisTemplate.opsForValue().increment(msgIdKey, refMsgId);
            refMsgId = rpcClient.getChatRpcService().updateAndGetRefMsgId(fromId, toId, refMsgIdStep, refMsgId);
            ctx.channel().attr(attributeKey).set(refMsgId);
        }
        else if (refMsgId - msgId < refMsgIdStep / 2) { //msgId自增到一定程度，refMsgId需要更新
            refMsgId = rpcClient.getChatRpcService().updateAndGetRefMsgId(fromId, toId, refMsgIdStep, refMsgId);
            ctx.channel().attr(attributeKey).set(refMsgId);
        }

        saveChat(msg, msgId); //消息入库，当前采用服务方异步入库，因此不支持等待回调结果。
        sendDeliveredMsg(ctx, msg, sessionId, msgId); //回复已送达

        // 扩散给自己的其他客户端
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

        if (fromId.equals(toId)) { //自发自收，只同步（上面逻辑就是同步），不发送（不给toId发送）
            return;
        }

        // 扩散给接收端的客户端
        Set<Object> toOnlineClients = queryOnlineClient(toId);
        toOnlineClients.remove(CommonUtil.conUniqueId(fromId, fromClient));
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

    private void saveChat(Msg msg, long msgId) {
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("fromId", msg.getBody().getFromId());
        msgMap.put("fromClient", msg.getBody().getFromClient());
        msgMap.put("toId", msg.getBody().getToId());
        msgMap.put("msgId", msgId);
        msgMap.put("msgType", msg.getHeader().getMsgType().getNumber());
        msgMap.put("content", msg.getBody().getContent());
        msgMap.put("msgTime", new Date());
        rpcClient.getChatRpcService().asyncSaveChat(msgMap);
    }

    private void sendDeliveredMsg(ChannelHandlerContext ctx, Msg msg, String sessionId, Long msgId) {
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
