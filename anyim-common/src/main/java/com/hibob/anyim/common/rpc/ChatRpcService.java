package com.hibob.anyim.common.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ChatRpcService {
    long refMsgId(String fromId, String toId, int refMsgIdDefault);

    long refMsgId(long groupId, int refMsgIdDefault);

    long updateAndGetRefMsgId(String fromId, String toId, int refMsgIdStep, long curRefMsgId);

    long updateAndGetRefMsgId(long groupId, int refMsgIdStep, long curRefMsgId);

    /**
     * chat消息异步入库
     * @param msg
     * @return
     */
    void asyncSaveChat(Map<String, Object> msg);

    /**
     * chat消息同步入库
     * @param msg
     * @return
     */
    boolean saveChat(Map<String, Object> msg);

    /**
     * group chat消息异步入库
     * @param msg
     * @return
     */
    void asyncSaveGroupChat(Map<String, Object> msg);

    /**
     * group chat消息同步入库
     * @param msg
     * @return
     */
    boolean saveGroupChat(Map<String, Object> msg);
}
