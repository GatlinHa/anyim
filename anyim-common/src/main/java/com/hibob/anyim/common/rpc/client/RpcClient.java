package com.hibob.anyim.common.rpc.client;

import com.hibob.anyim.common.rpc.service.ChatRpcService;
import com.hibob.anyim.common.rpc.service.GroupMngRpcService;
import com.hibob.anyim.common.rpc.service.UserRpcService;
import lombok.Data;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Data
@Component
public class RpcClient {

    @DubboReference(check = false, timeout = 3000) //关闭启动检查，否则启动会依赖RPC服务端
    private UserRpcService userRpcService;

    @DubboReference(check = false, timeout = 3000) //关闭启动检查，否则启动会依赖RPC服务端
    private ChatRpcService chatRpcService;

    @DubboReference(check = false, timeout = 3000) //关闭启动检查，否则启动会依赖RPC服务端
    private GroupMngRpcService groupMngRpcService;

}
