package com.hibob.anyim.netty.server.handler;

import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.enums.ConnectStatus;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.netty.protobuf.*;
import com.hibob.anyim.netty.server.processor.MsgProcessor;
import com.hibob.anyim.netty.server.processor.ProcessorFactory;
import com.hibob.anyim.netty.utils.SpringContextUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import static com.hibob.anyim.common.constants.Const.SPLIT_V;
import static com.hibob.anyim.netty.server.ws.WebSocketServer.getLocalRoute;


@Slf4j
public class MsgServerHandler extends SimpleChannelInboundHandler<Msg> {

    int readIdleTime = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
        log.info("MsgServerHandler receive a Msg:\n{}", msg);
        readIdleTime = 0;

        if (!validateMagic(ctx, msg)) return;

        MsgType msgType = msg.getHeader().getMsgType();
        MsgProcessor processor = ProcessorFactory.getProcessor(msgType);
        processor.process(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
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
                        log.error("close channel failed. cause is {}", future.cause());
                    }
                });
            }
        }
        else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uniqueId = (String) ctx.channel().attr(AttributeKey.valueOf(Const.UNIQUE_ID)).get();
        String account = uniqueId.split(SPLIT_V)[0];
        RpcClient rpcClient = SpringContextUtil.getBean("rpcClient");
        rpcClient.getUserRpcService().updateUserStatus(account, uniqueId, ConnectStatus.OFFLINE);
        clearCache(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MsgServerHandler catch a exception: ", cause);
        super.exceptionCaught(ctx, cause);
    }

    private boolean validateMagic(ChannelHandlerContext ctx, Msg msg) {
        int magic = msg.getHeader().getMagic();
        if (magic != Const.MAGIC) {
            // 非法消息，直接关闭连接
            log.error("error magic.");
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
                    log.error("close channel failed. cause is {}", future.cause());
                }
            });
            return false;
        }
        return true;
    }

    private void clearCache(ChannelHandlerContext ctx) {
        String uniqueId = (String) ctx.channel().attr(AttributeKey.valueOf(Const.UNIQUE_ID)).get();
        if (!StringUtils.hasLength(uniqueId))
        {
            return;
        }

        String account = uniqueId.split(SPLIT_V)[0];
        String routeKey = RedisKey.NETTY_GLOBAL_ROUTE + uniqueId;
        String onlineKey = RedisKey.NETTY_ONLINE_CLIENT + account;
        RedisTemplate<String, Object> redisTemplate = SpringContextUtil.getBean("redisTemplate");
        redisTemplate.delete(routeKey);
        redisTemplate.opsForSet().remove(onlineKey, uniqueId);
        getLocalRoute().remove(routeKey);
    }

}

