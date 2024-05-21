package com.hibob.anyim.netty.server.handler;

import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.utils.MsgUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ChannelHandler.Sharable
public class MsgHandler extends SimpleChannelInboundHandler<Msg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
        log.info("MsgHandlerFromSimpleChannelInboundHandler receive a Msg=============>");
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
        //TODO 真正的业务处理

        ctx.channel().writeAndFlush(MsgUtil.getMsg(
                0x12345678,
                0,
                0,
                false,
                1,
                1,
                2,
                2,
                1,
                "收到消息：" + msg.getBody().getContent()));
    }

}

