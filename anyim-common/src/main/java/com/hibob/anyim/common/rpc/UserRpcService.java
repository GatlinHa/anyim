package com.hibob.anyim.common.rpc;

import java.util.List;

public interface UserRpcService {
    List<String> queryOnline(String account);
}
