package com.hibob.anyim.netty.rpc;

import com.hibob.anyim.common.rpc.ChatRpcService;
import com.hibob.anyim.common.rpc.UserRpcService;
import lombok.Data;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Data
@Component
public class RpcClient {

    @DubboReference
    private UserRpcService userRpcService;

    @DubboReference
    private ChatRpcService chatRpcService;

}
