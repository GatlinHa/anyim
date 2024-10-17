package com.hibob.anyim.common.rpc.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ChatRpcService {
    long refMsgId(String session, int refMsgIdDefault);

    long updateAndGetRefMsgId(String session, int refMsgIdStep, long curRefMsgId);

    /**
     * 消息异步入库
     * @param msg
     * @return
     */
    void asyncSaveMsg(Map<String, Object> msg);

    /**
     * 消息同步入库
     * @param msg
     * @return
     */
    boolean saveMsg(Map<String, Object> msg);

    boolean updateReadMsgId(Map<String, Object> map);

}
