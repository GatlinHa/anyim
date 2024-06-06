package com.hibob.anyim.netty.rpc;

import com.hibob.anyim.common.rpc.ChatRpcService;
import com.hibob.anyim.common.rpc.UserRpcService;
import lombok.Data;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Data
@Component
public class RpcClient {

    @DubboReference(timeout = 60000) // TODO 调试过程中，超时时间设置长一点
    private UserRpcService userRpcService;

    @DubboReference(timeout = 60000) // TODO 调试过程中，超时时间设置长一点
    private ChatRpcService chatRpcService;

}
