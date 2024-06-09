package com.hibob.anyim.common.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ChatRpcService {
    long refMsgId(String fromId, String toId, int refMsgIdDefault);

    long refMsgId(String groupId, int refMsgIdDefault);

    long updateAndGetRefMsgId(String fromId, String toId, int refMsgIdStep, long curRefMsgId);

    long updateAndGetRefMsgId(String groupId, int refMsgIdStep, long curRefMsgId);

    void asyncSaveChat(Map<String, Object> msg);

    void asyncSaveGroupChat(Map<String, Object> msg);
}
