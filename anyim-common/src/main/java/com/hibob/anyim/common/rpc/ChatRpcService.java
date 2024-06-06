package com.hibob.anyim.common.rpc;

import java.util.Map;

public interface ChatRpcService {
    long refMsgId(String fromId, String toId, int refMsgIdDefault);

    long updateAndGetRefMsgId(String fromId, String toId, int refMsgIdStep, long curRefMsgId);

    void chatSave(Map<String, Object> msg);

    void groupChatSave(Map<String, Object> msg);
}
