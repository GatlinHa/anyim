package com.hibob.anyim.netty.client.handler;

import com.hibob.anyim.netty.constants.Const;
import com.hibob.anyim.netty.protobuf.Header;
import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.protobuf.MsgType;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<Msg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
        log.info("WebSocketClientHandler receive a Msg: {}", msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE.equals(evt)) {
            log.info("handshake success!");
            Header header = Header.newBuilder()
                    .setMagic(Const.MAGIC)
                    .setVersion(0)
                    .setMsgType(MsgType.HELLO)
                    .setIsExtension(false)
                    .build();
            Msg msg = Msg.newBuilder().setHeader(header).build();
            ctx.channel().writeAndFlush(msg).addListener(future -> {
               if (future.isSuccess()) {
                   log.info("send hello success!");
               }
               else {
                   log.info("send hello fail! cause is {}", future.cause());
               }
            });

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("WebSocketClientHandler exceptionCaught {}", cause.toString());
    }
}
