package com.hibob.anyim.netty.rpc;

import com.hibob.anyim.common.protobuf.MsgType;
import com.hibob.anyim.common.rpc.service.NettyRpcService;
import com.hibob.anyim.netty.rpc.processor.SystemMsgProcessor;
import com.hibob.anyim.netty.rpc.processor.SystemMsgProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class NettyRpcServiceImpl implements NettyRpcService {
    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void sendSysMsg(Map<String, Object> msgMap) {
        CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> handle(msgMap) ? 1 : 0, threadPoolExecutor);
        future.whenComplete((result, throwable) -> {
            log.info("NettyRpcServiceImpl::sendSysMsg Complete : {}", result);
            if (throwable != null) {
                log.error("exception: {}", throwable.getCause());
            }
        });
    }

    private boolean handle(Map<String, Object> msgMap) {
        MsgType msgType = MsgType.valueOf((Integer) msgMap.get("msgType"));
        SystemMsgProcessor processor = SystemMsgProcessorFactory.getProcessor(msgType);
        if (processor != null) {
            try {
                processor.processSystemMsg(msgMap);
                return true;
            } catch (Exception e) {
                log.error("system msg {} handle exception: {}", msgType, e.getMessage());
                return false;
            }
        }
        else {
            return false;
        }
    }
}
