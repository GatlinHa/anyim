package com.hibob.anyim.common.rpc.service;

import java.util.Map;

public interface NettyRpcService {
    /**
     * 发送系统消息给用户（异步）
     * @param msg 系统消息
     * @return
     */
    void sendSysMsg(Map<String, Object> msg);
}
