package com.hibob.anyim.common.rpc.service;

import java.util.List;
import java.util.Map;

public interface GroupMngRpcService {
    Map<String, Object> queryGroupInfo(String groupId);

    Map<String, Map<String, Object>> queryGroupInfoBatch(List<String> groupIdList);

    /**
     * 查询所有群成员
     * @param groupId
     * @return
     */
    List<String> queryGroupMembers(String groupId);

    /**
     * 查询群成员，刨除自己
     * @param groupId
     * @param account
     * @return
     */
    List<String> queryGroupMembers(String groupId, String account);

    boolean isMemberInGroup(String groupId, String account);
}
