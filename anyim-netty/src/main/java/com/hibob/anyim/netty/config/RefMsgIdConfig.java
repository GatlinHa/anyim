package com.hibob.anyim.netty.config;

import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.rpc.client.RpcClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Data
public class RefMsgIdConfig {
    @Value("${custom.ref-msg-id.default:10000}")
    private int refMsgIdDefault;

    @Value("${custom.ref-msg-id.step:10000}")
    private int refMsgIdStep;

    private final RpcClient rpcClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private Map<String, Long> refMsgIdMap = new HashMap<>();

    /**
     * 生成msgId
     * @param sessionId
     * @return
     */
    public long generateMsgId(String sessionId) {
        String msgIdKey = RedisKey.NETTY_REF_MSG_ID + sessionId;
        long refMsgId = getRefMsgId(sessionId);
        Long msgId = redisTemplate.opsForValue().increment(msgIdKey);
        if (msgId < refMsgIdDefault) { //应对第一条消息，redis重启等场景
            msgId = redisTemplate.opsForValue().increment(msgIdKey, refMsgIdStep);
            refMsgId = rpcClient.getChatRpcService().updateAndGetRefMsgId(sessionId, refMsgIdStep, refMsgId);
            refMsgIdMap.put(sessionId, refMsgId);
        }
        else if (refMsgId - msgId < refMsgIdStep / 2) { //msgId自增到一定程度，refMsgId需要更新
            refMsgId = rpcClient.getChatRpcService().updateAndGetRefMsgId(sessionId, refMsgIdStep, refMsgId);
            refMsgIdMap.put(sessionId, refMsgId);
        }
        return msgId;
    }


    private long getRefMsgId(String sessionId) {
        if (!refMsgIdMap.containsKey(sessionId) || refMsgIdMap.get(sessionId) < refMsgIdDefault) {
            long refMsgId = rpcClient.getChatRpcService().refMsgId(sessionId, refMsgIdDefault);
            refMsgIdMap.put(sessionId, refMsgId);
            return refMsgId;
        } else {
            return refMsgIdMap.get(sessionId);
        }
    }

}
