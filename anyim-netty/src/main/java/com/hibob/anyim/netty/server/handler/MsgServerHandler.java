package com.hibob.anyim.netty.server.handler;

import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.netty.constants.Const;
import com.hibob.anyim.netty.protobuf.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

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

        String token = (String) ctx.channel().attr(AttributeKey.valueOf(Const.AUTHORIZATION_KEY)).get();
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + token;
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
            ctx.channel().writeAndFlush(msgOut);
            ctx.channel().close().addListener(future -> {
                if (future.isSuccess()) {
                    redisTemplate.delete(routeKey);
                    getLocalRoute().remove(routeKey);
                    log.info("close channel success.");
                } else {
                    log.info("close channel failed. cause is {}", future.cause());
                }
            });
            return;
        }

        MsgType msgType = msg.getHeader().getMsgType();
        log.info("message type is: {}", msgType);
        switch (msgType) {
            case HELLO:
                redisTemplate.opsForValue().set(routeKey, "instanceid-xx", Duration.ofSeconds(Const.CHANNEL_EXPIRE)); //TODO "instanceid-xx"
                getLocalRoute().put(routeKey, ctx.channel());
                log.info("save route success. token is {}", token);
                break;
            case HEART_BEAT:
                // TODO 回复一个心跳
                break;
            case CHAT:
                // TODO
                break;
            case GROUP_CHAT:
                // TODO
                break;
            default:
                break;
        }

        Header header = Header.newBuilder()
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
                .setContent("收到消息类型是：" + msg.getHeader().getMsgType() + "，消息内容是：" + msg.getChatBody().getContent())
                .build();

        Msg msgOut = Msg.newBuilder().setHeader(header).setChatBody(body).build();
        log.info("MsgServerHandler send a response msg is: {}", msgOut);
        ctx.channel().writeAndFlush(msgOut);
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
            ctx.channel().writeAndFlush(msg);
            ctx.channel().close().addListener(future -> {
                if (future.isSuccess()) {
                    log.info("close channel success.");
                    // TODO 要删除路由表记录
                } else {
                    log.info("close channel failed. cause is {}", future.cause());
                }
            });
        }
    }

}

