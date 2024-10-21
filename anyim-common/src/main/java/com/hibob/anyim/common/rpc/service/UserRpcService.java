package com.hibob.anyim.common.rpc.service;

import com.hibob.anyim.common.enums.ConnectStatus;

import java.util.List;
import java.util.Map;

public interface UserRpcService {
    List<String> queryOnline(String account);

    Map<String, Object> queryUserInfo(String account);

    Map<String, Map<String, Object>> queryUserInfoBatch(List<String> accountList);

    boolean updateUserStatus(String account, String uniqueId, ConnectStatus status);

}
