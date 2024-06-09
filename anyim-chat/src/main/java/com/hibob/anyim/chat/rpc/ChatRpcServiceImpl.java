package com.hibob.anyim.chat.rpc;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.entity.MsgChat;
import com.hibob.anyim.chat.entity.MsgGroupChat;
import com.hibob.anyim.chat.entity.SessionChat;
import com.hibob.anyim.chat.mapper.MsgChatMapper;
import com.hibob.anyim.chat.mapper.MsgGroupChatMapper;
import com.hibob.anyim.chat.mapper.SessionChatMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.rpc.ChatRpcService;
import com.hibob.anyim.common.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
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
    private final SessionChatMapper sessionChatMapper;
    private final MsgChatMapper msgChatMapper;
    private final MsgGroupChatMapper msgGroupChatMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${custom.msg-ttl-in-redis:604800}")
    private int msgTtlInRedis;

    @Value("${custom.msg-capacity-in-redis:100000}")
    private int msgCapacityInRedis;

    @Override
    public long refMsgId(String fromId, String toId, int refMsgIdDefault) {
        // 通过fromId和toId查询sessionId
        SessionChat sessionChat = selectSessionChat(fromId, toId);
        if (sessionChat == null) {
            // 创建session;
            createSessionChat(fromId, toId, refMsgIdDefault);
            return refMsgIdDefault;
        }

        return sessionChat.getRefMsgId();
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
            msgChat.setContent((String) msg.get("content")); //TODO 内容要考虑加密
            msgChat.setMsgTime((Date) msg.get("msgTime"));
            // TODO 这个是要入MongoDB的，不是MySQL，暂时用mysql代替
            int insert = msgChatMapper.insert(msgChat);
            if (insert > 0) {
                insertToRedis((long) msg.get("msgId"), sessionId, msgChat);
            }
            else {
                log.error("asyncSaveChat insert failed, sessionId: {}, msgId: {}", sessionId, msg.get("msgId"));
            }

            return insert;
        }, threadPoolExecutor);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("asyncSaveChat execute exception: {}", throwable.getCause());
            }
        });
    }

    @Override
    public void asyncSaveGroupChat(Map<String, Object> msg) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            MsgGroupChat msgGroupChat = new MsgGroupChat();
            msgGroupChat.setSessionId((String) msg.get("group_id"));
            msgGroupChat.setFromId((String) msg.get("from_id"));
            msgGroupChat.setFromClient((String) msg.get("from_client"));
            msgGroupChat.setMsgId((long) msg.get("msg_id"));
            msgGroupChat.setMsgType((int) msg.get("msgType"));
            msgGroupChat.setContent((String) msg.get("content")); //TODO 内容要考虑加密
            msgGroupChat.setMsgTime((Date) msg.get("msgTime"));
            // TODO 这个是要入MongoDB的，不是MySQL，暂时用mysql代替
            int insert = msgGroupChatMapper.insert(msgGroupChat);
            if (insert > 0) {
                insertToRedis((long) msg.get("msgId"), (String)msg.get("group_id"), msgGroupChat);
            }
            else {
                log.error("asyncSaveChat insert failed, groupId: {}, msgId: {}", msg.get("group_id"), msg.get("msgId"));
            }

            return insert;
        }, threadPoolExecutor);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("asyncSaveGroupChat execute exception: {}", throwable.getCause());
            }
        });
    }

    private void insertToRedis(long msgId, String sessionId, Object value) {
        String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        redisTemplate.opsForZSet().add(key1, msgId, msgId);
        Long card = redisTemplate.opsForZSet().zCard(key1);
        if (card > msgCapacityInRedis) {
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

}
