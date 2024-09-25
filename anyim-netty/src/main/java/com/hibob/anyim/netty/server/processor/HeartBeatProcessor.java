package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.netty.protobuf.Header;
import com.hibob.anyim.netty.protobuf.Msg;
import com.hibob.anyim.netty.protobuf.MsgType;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class HeartBeatProcessor extends MsgProcessor{

    @Override
    public void process(ChannelHandlerContext ctx, Msg msg)  throws Exception{
        Header headerOut = Header.newBuilder()
                .setMagic(Const.MAGIC)
                .setVersion(0)
                .setMsgType(MsgType.HEART_BEAT)
                .build();
        Msg msgOut = Msg.newBuilder().setHeader(headerOut).build();
        ctx.writeAndFlush(msgOut);
    }
}
