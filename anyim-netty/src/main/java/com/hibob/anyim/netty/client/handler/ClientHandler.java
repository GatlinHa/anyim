package com.hibob.anyim.netty.client.handler;

import com.hibob.anyim.netty.protobuf.Msg;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ClientHandler extends SimpleChannelInboundHandler<Msg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
        log.info("WebSocketClientHandler receive a Msg=============>");
        log.info("magic: {}", msg.getHeader().getMagic());
        log.info("version: {}", msg.getHeader().getVersion());
        log.info("msgType: {}", msg.getHeader().getMsgType());
        log.info("isExtension: {}", msg.getHeader().getIsExtension());
        log.info("fromId: {}", msg.getBody().getFromId());
        log.info("fromDev: {}", msg.getBody().getFromDev());
        log.info("toId: {}", msg.getBody().getToId());
        log.info("toDev: {}", msg.getBody().getToDev());
        log.info("seq: {}", msg.getBody().getSeq());
        log.info("content: {}", msg.getBody().getContent());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE.equals(evt)) {
            log.info("handshake success!");
//            ctx.channel().writeAndFlush(new TextWebSocketFrame("你好呀，服务端！")).sync();
//            ctx.channel().writeAndFlush(MsgUtil.getMsg(0x12345678, 0, 0, false, 1, 1, 2, 2, 1, "你好呀！"));
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("WebSocketClientHandler exceptionCaught {}", cause.toString());
    }
}
