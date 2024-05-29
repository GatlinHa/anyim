package com.hibob.anyim.user.rpc;

import com.hibob.anyim.common.rpc.UserRpcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@DubboService
public class UserRpcServiceImpl implements UserRpcService {
    @Override
    public List<String> queryOnline(String account) {
        log.info("=================>服务端开始响应......");
        ArrayList<String> list = new ArrayList<>();
        list.add("hello");
        return list;
    }
}
