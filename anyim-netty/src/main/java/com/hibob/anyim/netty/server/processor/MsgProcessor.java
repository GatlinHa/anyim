package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.netty.protobuf.Msg;
import io.netty.channel.ChannelHandlerContext;

public interface MsgProcessor {
    void process(ChannelHandlerContext ctx, Msg msg) throws Exception;
}
