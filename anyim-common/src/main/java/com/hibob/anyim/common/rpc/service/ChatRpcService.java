package com.hibob.anyim.common.rpc.service;

import java.util.List;
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

    /**
     * 插入群session数据，支持批量插入
     * @param groupId 群组id
     * @param sessionList
     * @return
     */
    boolean insertGroupSessions(String groupId, List<Map<String, Object>> sessionList);

    /**
     * 用户离群时需要往session表中更新相关信息
     * @param list 需要更新的信息，1个用户1个元素，每个元素中包含：sessionId, account
     * @return
     */
    boolean updateGroupSessionsForLeave(List<Map<String, Object>> list);

}
