package com.hibob.anyim.common.rpc;

import java.util.List;
import java.util.Map;

public interface GroupMngRpcService {
    Map<String, Object> queryGroupInfo(long groupId);

    /**
     * 查询所有群成员
     * @param groupId
     * @return
     */
    List<String> queryGroupMembers(long groupId);

    /**
     * 查询群成员，刨除自己
     * @param groupId
     * @param account
     * @return
     */
    List<String> queryGroupMembers(long groupId, String account);
}
