package com.hibob.anyim.common.rpc.service;

import java.util.List;
import java.util.Map;

public interface GroupMngRpcService {
    Map<String, Object> queryGroupInfo(String groupId);

    Map<String, Map<String, Object>> queryGroupInfoBatch(List<String> groupIdList);

    /**
     * 查询群成员
     * @param groupId
     * @return
     */
    List<String> queryGroupMembers(String groupId);
}
