package com.hibob.anyim.netty.server.processor;

import com.hibob.anyim.netty.protobuf.MsgType;
import com.hibob.anyim.netty.utils.SpringContextUtil;

public class ProcessorFactory {

    public static MsgProcessor getProcessor(MsgType msgType) {
        switch (msgType) {
            case HELLO:
                return SpringContextUtil.getBean(HelloProcessor.class);
            case HEART_BEAT:
                return SpringContextUtil.getBean(HeartBeatProcessor.class);
            case CHAT:
                return SpringContextUtil.getBean(ChatProcessor.class);
            case READ:
                return SpringContextUtil.getBean(ReadProcessor.class);
            case GROUP_CHAT:
                return SpringContextUtil.getBean(GroupChatProcessor.class);
            default:
                return null;
        }
    }
}
