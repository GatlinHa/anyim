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
        String uniqueId = (String) ctx.channel().attr(AttributeKey.valueOf(Const.KEY_UNIQUE_ID)).get();
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + uniqueId;
        if (!validateMagic(ctx, msg, routeKey)) return;

        MsgType msgType = msg.getHeader().getMsgType();
        Header header;
        Msg msgOut;
        log.info("message type is: {}", msgType);
        switch (msgType) {
            case HELLO:
                redisTemplate.opsForValue().set(routeKey, "instanceid-xx", Duration.ofSeconds(Const.CHANNEL_EXPIRE)); //TODO "instanceid-xx"
                getLocalRoute().put(routeKey, ctx.channel());
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
                // TODO
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
                    log.info("close channel success.");
                    // TODO 要删除路由表记录
                } else {
                    log.info("close channel failed. cause is {}", future.cause());
                }
            });
        }
    }


    private boolean validateMagic(ChannelHandlerContext ctx, Msg msg, String routeKey) {
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
                    redisTemplate.delete(routeKey);
                    getLocalRoute().remove(routeKey);
                    log.info("close channel success.");
                } else {
                    log.info("close channel failed. cause is {}", future.cause());
                }
            });
            return false;
        }
        return true;
    }

}

