package com.hibob.anyim.netty.server.handler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.netty.config.NacosConfig;
import com.hibob.anyim.netty.constants.Const;
import com.hibob.anyim.netty.protobuf.*;
import com.hibob.anyim.netty.utils.SpringContextUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Set;

import static com.hibob.anyim.common.constants.Const.SPLIT_C;
import static com.hibob.anyim.common.constants.Const.SPLIT_V;
import static com.hibob.anyim.netty.server.ws.WebSocketServer.getLocalRoute;


@Slf4j
public class MsgServerHandler extends SimpleChannelInboundHandler<Msg> {

    private final RedisTemplate<String, Object> redisTemplate;
    int readIdleTime = 0;

    public MsgServerHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
        log.info("MsgServerHandler receive a Msg {}", msg);
        readIdleTime = 0;
        if (!validateMagic(ctx, msg)) return;

        MsgType msgType = msg.getHeader().getMsgType();
        Header header;
        Msg msgOut;
        log.info("message type is: {}", msgType);

        switch (msgType) {
            case HELLO:
                writeCache(ctx);
                header = Header.newBuilder()
                        .setMagic(Const.MAGIC)
                        .setVersion(0)
                        .setMsgType(MsgType.HELLO)
                        .build();
                msgOut = Msg.newBuilder().setHeader(header).build();
                ctx.writeAndFlush(msgOut);
                break;
            case HEART_BEAT:
                header = Header.newBuilder()
                        .setMagic(Const.MAGIC)
                        .setVersion(0)
                        .setMsgType(MsgType.HEART_BEAT)
                        .build();
                msgOut = Msg.newBuilder().setHeader(header).build();
                ctx.writeAndFlush(msgOut);
                break;
            case CHAT:
                // TODO 这里要校验接收端的netty实例在注册中心是否是有效的
//                nacosConfig.isValidInstance(instance);
                header = Header.newBuilder()
                        .setMagic(Const.MAGIC)
                        .setVersion(0)
                        .setMsgType(MsgType.CHAT)
                        .setIsExtension(false)
                        .build();
                ChatBody body = ChatBody.newBuilder()
                        .setFromId("1")
                        .setFromDev("1")
                        .setToId("2")
                        .setToDev("2")
                        .setSeq(1)
                        .setContent("test")
                        .build();
                msgOut = Msg.newBuilder().setHeader(header).setChatBody(body).build();
                ctx.writeAndFlush(msgOut);
                break;
            case GROUP_CHAT:
                // TODO
                break;
            default:
                break;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        switch (event.state()) {
            case READER_IDLE:
                readIdleTime++;
                log.info("readIdleTime is {}", readIdleTime);
                break;
            default:
                break;
        }

        if (readIdleTime > 3) {
            log.info("more than three time read idle event, will close channel.");
            Header header = Header.newBuilder()
                    .setMagic(Const.MAGIC)
                    .setVersion(0)
                    .setMsgType(MsgType.CLOSE_BY_READ_IDLE)
                    .setIsExtension(false)
                    .build();
            Msg msg = Msg.newBuilder().setHeader(header).build();
            ctx.writeAndFlush(msg);
            ctx.close().addListener(future -> {
                if (future.isSuccess()) {
                    clearCache(ctx);
                    log.info("close channel and clear cache success.");
                } else {
                    log.info("close channel failed. cause is {}", future.cause());
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clearCache(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MsgServerHandler catch a exception: ", cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }

    private boolean validateMagic(ChannelHandlerContext ctx, Msg msg) {
        String uniqueId = (String) ctx.channel().attr(AttributeKey.valueOf(Const.KEY_UNIQUE_ID)).get();
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + uniqueId;
        int magic = msg.getHeader().getMagic();
        if (magic != Const.MAGIC) {
            // 非法消息，直接关闭连接
            log.info("error magic.");
            Header header = Header.newBuilder()
                    .setMagic(Const.MAGIC)
                    .setVersion(0)
                    .setMsgType(MsgType.CLOSE_BY_ERROR_MAGIC)
                    .setIsExtension(false)
                    .build();
            Msg msgOut = Msg.newBuilder().setHeader(header).build();
            ctx.writeAndFlush(msgOut);
            ctx.close().addListener(future -> {
                if (future.isSuccess()) {
                    clearCache(ctx);
                    log.info("close channel success.");
                } else {
                    log.info("close channel failed. cause is {}", future.cause());
                }
            });
            return false;
        }
        return true;
    }

    private void writeCache(ChannelHandlerContext ctx) {
        NacosConfig nacosConfig = SpringContextUtil.getBean(NacosConfig.class);
        String uniqueId = (String) ctx.channel().attr(AttributeKey.valueOf(Const.KEY_UNIQUE_ID)).get();
        String account = uniqueId.split(SPLIT_V)[0];
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + uniqueId;
        String onlineKey = RedisKey.NETTY_ONLINE_CLIENT + account;
        String instance = CommonUtil.getLocalIp() + SPLIT_C + nacosConfig.getPort();
        redisTemplate.opsForValue().set(routeKey, instance, Duration.ofSeconds(Const.CHANNEL_EXPIRE));
        redisTemplate.opsForSet().add(onlineKey, uniqueId);
        redisTemplate.expire(onlineKey, Duration.ofSeconds(Const.CACHE_ONLINE_EXPIRE));
        getLocalRoute().put(routeKey, ctx.channel());
    }

    private void clearCache(ChannelHandlerContext ctx) {
        String uniqueId = (String) ctx.channel().attr(AttributeKey.valueOf(Const.KEY_UNIQUE_ID)).get();
        String account = uniqueId.split(SPLIT_V)[0];
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + uniqueId;
        String onlineKey = RedisKey.NETTY_ONLINE_CLIENT + account;
        redisTemplate.delete(routeKey);
        redisTemplate.opsForSet().remove(onlineKey, uniqueId);
        getLocalRoute().remove(routeKey);
    }

    private Set<Object> queryOnlineClient(String account) {
        String onlineKey = RedisKey.NETTY_ONLINE_CLIENT + account;
        Set<Object> members = redisTemplate.opsForSet().members(onlineKey);
        if (members.size() == 0) {
            // TODO 向USER发送一个RPC调用，从数据库查询在线的客户端

        }

        return members;
    }

}

