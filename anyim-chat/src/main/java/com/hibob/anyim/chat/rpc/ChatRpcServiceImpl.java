package com.hibob.anyim.chat.rpc;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.entity.*;
import com.hibob.anyim.chat.mapper.SessionChatMapper;
import com.hibob.anyim.chat.mapper.SessionGroupChatMapper;
import com.hibob.anyim.chat.mapper.SessionMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.rpc.service.ChatRpcService;
import com.hibob.anyim.common.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.hibob.anyim.common.utils.CommonUtil.combineId;
import static com.hibob.anyim.common.utils.CommonUtil.sortId;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class ChatRpcServiceImpl implements ChatRpcService {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final SessionMapper sessionMapper;
    private final SessionChatMapper sessionChatMapper;
    private final SessionGroupChatMapper sessionGroupChatMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    @Value("${custom.msg-ttl-in-redis:604800}")
    private int msgTtlInRedis;

    @Value("${custom.msg-capacity-in-redis:10000}")
    private int msgCapacityInRedis;

    @Override
    public long refMsgId(String fromId, String toId, int refMsgIdDefault) {
        SessionChat sessionChat = selectSessionChat(fromId, toId);
        if (sessionChat == null) {
            // 创建session;
            createSessionChat(fromId, toId, refMsgIdDefault);
            return refMsgIdDefault;
        }

        return sessionChat.getRefMsgId();
    }

    @Override
    public long refMsgId(long groupId, int refMsgIdDefault) {
        SessionGroupChat sessionGroupChat = selectSessionGroupChat(groupId);
        if (sessionGroupChat == null) {
            // 创建session;
            createSessionGroupChat(groupId, refMsgIdDefault);
            return refMsgIdDefault;
        }

        return sessionGroupChat.getRefMsgId();
    }

    @Override
    public long updateAndGetRefMsgId(String fromId, String toId, int refMsgIdStep, long curRefMsgId) {
        String[] sorted = sortId(fromId, toId);
        long newRefMsgId = curRefMsgId + refMsgIdStep;
        LambdaUpdateWrapper<SessionChat> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(SessionChat::getUserA, sorted[0])
                .eq(SessionChat::getUserB, sorted[1])
                .eq(SessionChat::getRefMsgId, curRefMsgId) // 乐观锁
                .set(SessionChat::getRefMsgId, newRefMsgId);
        sessionChatMapper.update(updateWrapper);
        return selectSessionChat(fromId, toId).getRefMsgId();
    }

    @Override
    public long updateAndGetRefMsgId(long groupId, int refMsgIdStep, long curRefMsgId) {
        long newRefMsgId = curRefMsgId + refMsgIdStep;
        LambdaUpdateWrapper<SessionGroupChat> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(SessionGroupChat::getGroupId, groupId)
                .eq(SessionGroupChat::getRefMsgId, curRefMsgId) // 乐观锁
                .set(SessionGroupChat::getRefMsgId, newRefMsgId);
        sessionGroupChatMapper.update(updateWrapper);
        return selectSessionGroupChat(groupId).getRefMsgId();
    }

    @Override
    public void asyncSaveChat(Map<String, Object> msg) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            String fromId = (String) msg.get("fromId");
            String toId = (String) msg.get("toId");
            String[] sorted = sortId(fromId, toId);
            String sessionId = combineId(sorted[0], sorted[1]);

            MsgChat msgChat = new MsgChat();
            msgChat.setSessionId(sessionId);
            msgChat.setFromId(fromId);
            msgChat.setFromClient((String) msg.get("fromClient"));
            msgChat.setToId(toId);
            msgChat.setMsgId((long) msg.get("msgId"));
            msgChat.setMsgType((int) msg.get("msgType"));
            msgChat.setContent((String) msg.get("content")); //客户端负责加密内容
            msgChat.setMsgTime((Date) msg.get("msgTime"));
            msgChat.setCreateTime(new Date());
            MsgChat insert = mongoTemplate.insert(msgChat);
            if (insert.getId() != null) { //如果入库成功，id会有值
                insertToRedis((long) msg.get("msgId"), sessionId, msgChat);
            }
            else {
                log.error("asyncSaveChat insert failed, sessionId: {}, msgId: {}", sessionId, msg.get("msgId"));
                return 0;
            }

            // 更新anyim_chat_session表，fromid用户的在这个session中的last_msg_id应该是最后一条（即本条消息的msgId）
            LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(Session::getAccount, fromId)
                    .eq(Session::getSessionId, sessionId);
            updateWrapper.set(Session::getLastMsgId, msg.get("msgId"));
            updateWrapper.set(Session::getLastMsgTime, new Date());
            sessionMapper.update(updateWrapper);

            return 1;
        }, threadPoolExecutor);

        future.whenComplete((result, throwable) -> {
            log.info("==================>asyncSaveChat execute result: {}", result);
            if (throwable != null) {
                log.error("asyncSaveChat execute exception: {}", throwable.getCause());
            }
        });
    }

    @Override
    public boolean saveChat(Map<String, Object> msg) {
        String fromId = (String) msg.get("fromId");
        String toId = (String) msg.get("toId");
        String[] sorted = sortId(fromId, toId);
        String sessionId = combineId(sorted[0], sorted[1]);

        MsgChat msgChat = new MsgChat();
        msgChat.setSessionId(sessionId);
        msgChat.setFromId(fromId);
        msgChat.setFromClient((String) msg.get("fromClient"));
        msgChat.setToId(toId);
        msgChat.setMsgId((long) msg.get("msgId"));
        msgChat.setMsgType((int) msg.get("msgType"));
        msgChat.setContent((String) msg.get("content")); //客户端负责加密内容
        msgChat.setMsgTime((Date) msg.get("msgTime"));
        msgChat.setCreateTime(new Date());
        MsgChat insert = mongoTemplate.insert(msgChat);
        if (insert.getId() != null) { //如果入库成功，id会有值
            insertToRedis((long) msg.get("msgId"), sessionId, msgChat);
            return true;
        }
        else {
            log.error("saveChat insert failed, sessionId: {}, msgId: {}", sessionId, msg.get("msgId"));
            return false;
        }
    }

    @Override
    public void asyncSaveGroupChat(Map<String, Object> msg) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            MsgGroupChat msgGroupChat = new MsgGroupChat();
            msgGroupChat.setSessionId(String.valueOf(msg.get("groupId")));
            msgGroupChat.setFromId((String) msg.get("fromId"));
            msgGroupChat.setFromClient((String) msg.get("fromClient"));
            msgGroupChat.setGroupId((Long) msg.get("groupId"));
            msgGroupChat.setMsgId((long) msg.get("msgId"));
            msgGroupChat.setMsgType((int) msg.get("msgType"));
            msgGroupChat.setContent((String) msg.get("content")); //客户端负责加密内容
            msgGroupChat.setMsgTime((Date) msg.get("msgTime"));
            MsgGroupChat insert = mongoTemplate.insert(msgGroupChat);
            if (insert.getId() != null) {
                insertToRedis((long) msg.get("msgId"), String.valueOf(msg.get("groupId")), msgGroupChat);
            }
            else {
                log.error("asyncSaveGroupChat insert failed, groupId: {}, msgId: {}", msg.get("groupId"), msg.get("msgId"));
                return 0;
            }

            return 1;
        }, threadPoolExecutor);

        future.whenComplete((result, throwable) -> {
            log.info("==================>asyncSaveGroupChat execute result: {}", result);
            if (throwable != null) {
                log.error("asyncSaveGroupChat execute exception: {}", throwable.getCause());
            }
        });
    }

    @Override
    public boolean saveGroupChat(Map<String, Object> msg) {
        MsgGroupChat msgGroupChat = new MsgGroupChat();
        msgGroupChat.setSessionId((String) msg.get("group_id"));
        msgGroupChat.setFromId((String) msg.get("from_id"));
        msgGroupChat.setFromClient((String) msg.get("from_client"));
        msgGroupChat.setMsgId((long) msg.get("msg_id"));
        msgGroupChat.setMsgType((int) msg.get("msgType"));
        msgGroupChat.setContent((String) msg.get("content")); //客户端负责加密内容
        msgGroupChat.setMsgTime((Date) msg.get("msgTime"));
        MsgGroupChat insert = mongoTemplate.insert(msgGroupChat);
        if (insert.getId() != null) {
            insertToRedis((long) msg.get("msgId"), (String)msg.get("group_id"), msgGroupChat);
            return true;
        }
        else {
            log.error("saveGroupChat insert failed, groupId: {}, msgId: {}", msg.get("group_id"), msg.get("msgId"));
            return false;
        }
    }

    private void insertToRedis(long msgId, String sessionId, Object value) {
        String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        redisTemplate.opsForZSet().add(key1, msgId, msgId);
        Long card = redisTemplate.opsForZSet().zCard(key1);
        if (card > msgCapacityInRedis) {
            // 超出限制，删除10%的数据
            redisTemplate.opsForZSet().removeRange(key1, 0, msgCapacityInRedis / 10);
        }
        String key2 = RedisKey.CHAT_SESSION_MSG_ID_MSG + sessionId + Const.SPLIT_C + msgId;
        redisTemplate.opsForValue().set(key2, JSON.toJSONString(value), Duration.ofSeconds(msgTtlInRedis));
    }

    private void createSessionChat(String fromId, String toId, int refMsgIdDefault) {
        String[] sorted = sortId(fromId, toId);
        SessionChat sessionChat = new SessionChat();
        sessionChat.setSessionId(CommonUtil.combineId(sorted[0], sorted[1]));
        sessionChat.setUserA(sorted[0]);
        sessionChat.setUserB(sorted[1]);
        sessionChat.setRefMsgId(refMsgIdDefault);
        sessionChatMapper.insert(sessionChat);
    }

    private void createSessionGroupChat(long groupId, int refMsgIdDefault) {
        SessionGroupChat sessionGroupChat = new SessionGroupChat();
        sessionGroupChat.setSessionId(String.valueOf(groupId));
        sessionGroupChat.setGroupId(groupId);
        sessionGroupChat.setRefMsgId(refMsgIdDefault);
        sessionGroupChatMapper.insert(sessionGroupChat);
    }

    private SessionChat selectSessionChat(String fromId, String toId) {
        String[] sorted = sortId(fromId, toId);
        LambdaQueryWrapper<SessionChat> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SessionChat::getUserA, sorted[0]).
                eq(SessionChat::getUserB, sorted[1]);
        List<SessionChat> sessionChat = sessionChatMapper.selectList(queryWrapper);
        if (sessionChat.size() > 0) {
            return sessionChat.get(0);
        }
        else {
            return null;
        }
    }

    private SessionGroupChat selectSessionGroupChat(long groupId) {
        LambdaQueryWrapper<SessionGroupChat> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SessionGroupChat::getSessionId, groupId);
        List<SessionGroupChat> sessionGroupChat = sessionGroupChatMapper.selectList(queryWrapper);
        if (sessionGroupChat.size() > 0) {
            return sessionGroupChat.get(0);
        }
        else {
            return null;
        }
    }

}
