package com.hibob.anyim.netty.server.handler;

import com.hibob.anyim.netty.constants.Const;
import com.hibob.anyim.netty.enums.MsgType;
import com.hibob.anyim.netty.protobuf.Body;
import com.hibob.anyim.netty.protobuf.Header;
import com.hibob.anyim.netty.protobuf.Msg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class MsgServerHandler extends SimpleChannelInboundHandler<Msg> {
    int readIdleTime = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
        log.info("MsgHandlerFromSimpleChannelInboundHandler receive a Msg {}", msg);
        readIdleTime = 0;
        //TODO 真正的业务处理

        int magic = msg.getHeader().getMagic();
        if (magic != Const.MAGIC) {

        }

        Header header = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0)
                .setMsgType(MsgType.CHAT.code())
                .setIsExtension(false)
                .build();
        Body body = Body.newBuilder()
                .setFromId(1)
                .setFromDev(1)
                .setToId(2)
                .setToDev(2)
                .setSeq(1)
                .setContent("收到消息：" + msg.getBody().getContent())
                .build();
        Msg msgOut = Msg.newBuilder().setHeader(header).setBody(body).build();

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
                    .setMsgType(MsgType.CLOSE_BY_READ_IDLE.code())
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

