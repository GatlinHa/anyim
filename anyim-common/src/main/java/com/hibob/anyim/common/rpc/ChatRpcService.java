package com.hibob.anyim.common.rpc;

public interface ChatRpcService {
    long refMsgId(String fromId, String toId, int refMsgIdDefault);

    long updateAndGetRefMsgId(String fromId, String toId, int refMsgIdStep, long curRefMsgId);
}
