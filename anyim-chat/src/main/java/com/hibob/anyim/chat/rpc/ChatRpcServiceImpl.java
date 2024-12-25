package com.hibob.anyim.chat.rpc;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.entity.*;
import com.hibob.anyim.chat.mapper.RefMsgIdMapper;
import com.hibob.anyim.chat.mapper.SessionMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.protobuf.MsgType;
import com.hibob.anyim.common.rpc.service.ChatRpcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class ChatRpcServiceImpl implements ChatRpcService {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final SessionMapper sessionMapper;
    private final RefMsgIdMapper refMsgIdMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    @Value("${custom.msg-ttl-in-redis:604800}")
    private int msgTtlInRedis;

    @Value("${custom.msg-capacity-in-redis:10000}")
    private int msgCapacityInRedis;

    @Override
    public long refMsgId(String session, int refMsgIdDefault) {
        RefMsgId refMsgId = selectRefMsgId(session);
        if (refMsgId == null) {
            // 创建session;
            createRefMsgId(session, refMsgIdDefault);
            return refMsgIdDefault;
        }

        return refMsgId.getRefMsgId();
    }

    @Override
    public long updateAndGetRefMsgId(String session, int refMsgIdStep, long curRefMsgId) {
        long newRefMsgId = curRefMsgId + refMsgIdStep;
        LambdaUpdateWrapper<RefMsgId> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(RefMsgId::getSessionId, session)
                .eq(RefMsgId::getRefMsgId, curRefMsgId) // 乐观锁
                .set(RefMsgId::getRefMsgId, newRefMsgId);
        refMsgIdMapper.update(updateWrapper);
        return selectRefMsgId(session).getRefMsgId();
    }

    @Override
    public void asyncSaveMsg(Map<String, Object> msg) {
        CompletableFuture<Integer> future =
                CompletableFuture.supplyAsync(() -> saveMsg(msg) ? 1 : 0, threadPoolExecutor);
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("asyncSaveChat execute exception: {}", throwable.getCause());
            }
        });
    }

    @Override
    public boolean saveMsg(Map<String, Object> msg) {
        String sessionId = (String) msg.get("sessionId");
        MsgDb msgDb = new MsgDb();
        msgDb.setSessionId(sessionId);
        msgDb.setFromId((String) msg.get("fromId"));
        msgDb.setFromClient((String) msg.get("fromClient"));
        msgDb.setRemoteId((String) msg.get("remoteId"));
        msgDb.setMsgId((long) msg.get("msgId"));
        msgDb.setMsgType((int) msg.get("msgType"));
        msgDb.setContent((String) msg.get("content")); //客户端负责加密内容
        msgDb.setMsgTime((Date) msg.get("msgTime"));
        msgDb.setCreateTime(new Date());
        MsgDb insert = mongoTemplate.insert(msgDb);
        if (insert.getId() != null) { //如果入库成功，id会有值
            insertToRedis((long) msg.get("msgId"), sessionId, msgDb);
            return true;
        }
        else {
            return false;
        }
    }

    private void insertToRedis(long msgId, String sessionId, Object value) {
        String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        redisTemplate.opsForZSet().add(key1, msgId + Const.SPLIT_V + new Date().getTime(), msgId);
        Long card = redisTemplate.opsForZSet().zCard(key1);
        if (card > msgCapacityInRedis) {
            // 超出限制，删除10%的数据
            redisTemplate.opsForZSet().removeRangeByScore(key1, 0, msgCapacityInRedis / 10);
        }
        String key2 = RedisKey.CHAT_SESSION_MSG + sessionId + Const.SPLIT_C + msgId;
        redisTemplate.opsForValue().set(key2, JSON.toJSONString(value), Duration.ofSeconds(msgTtlInRedis));
    }

    private void createRefMsgId(String session, int refMsgIdDefault) {
        RefMsgId refMsgId = new RefMsgId();
        refMsgId.setSessionId(session);
        refMsgId.setRefMsgId(refMsgIdDefault);
        refMsgIdMapper.insert(refMsgId);
    }

    private RefMsgId selectRefMsgId(String session) {
        LambdaQueryWrapper<RefMsgId> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(RefMsgId::getSessionId, session);
        List<RefMsgId> refMsgId = refMsgIdMapper.selectList(queryWrapper);
        if (refMsgId.size() > 0) {
            return refMsgId.get(0);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean updateReadMsgId(Map<String, Object> map) {
        LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Session::getAccount, map.get("account"))
                .eq(Session::getSessionId, map.get("sessionId"))
                .set(Session::getReadMsgId, map.get("readMsgId"))
                .set(Session::getReadTime, map.get("readTime"));
        return sessionMapper.update(updateWrapper) > 0;
    }

    @Override
    @Transactional
    public boolean insertGroupSessions(List<Map<String, Object>> sessionList) {
        sessionMapper.batchInsertOrUpdate(sessionList); // 先插入
        sessionMapper.batchUpdateForJoin(sessionList);  // 后更新
        return true;
    }

    @Override
    public boolean updateGroupSessionsForLeave(List<Map<String, Object>> sessionList) {
        return sessionMapper.batchUpdateForLeave(sessionList) > 0;
    }

}
