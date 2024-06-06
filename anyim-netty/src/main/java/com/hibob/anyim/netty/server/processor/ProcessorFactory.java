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
            case READ: // 和ChatProcessor相同处理逻辑，目前来看是这样
                return SpringContextUtil.getBean(ChatProcessor.class);
            case GROUP_CHAT:
                return null; //TODO
            default:
                return null;
        }
    }
}
