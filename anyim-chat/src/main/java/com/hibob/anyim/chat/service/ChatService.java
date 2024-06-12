package com.hibob.anyim.chat.service;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.chat.dto.request.PullChatMsgReq;

import com.hibob.anyim.chat.entity.MsgChat;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.session.ReqSession;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.common.utils.ResultUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${custom.msg-ttl-in-redis:604800}")
    private int msgTtlInRedis;

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    public ResponseEntity<IMHttpResponse> pullMsg(PullChatMsgReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String toAccount = dto.getToAccount();
        String sessionId = CommonUtil.combineId(account, toAccount);
        long lastMsgId = dto.getLastMsgId();
        long lastPullTime = dto.getLastPullTime();
        long currentTime = new Date().getTime() / 1000;

        if (currentTime - lastPullTime < msgTtlInRedis) { // 7天内查询Redis
            // 第1次查询缓存，获取sessionId下面的msgId集合
            String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
            Set<Object> msgIds = redisTemplate.opsForZSet().rangeByScore(key1, lastMsgId + 1, Double.MAX_VALUE, 0, 1000);
            List<MsgChat> msglist = new ArrayList<>();
            if (msgIds.size() == 0) {
                return ResultUtil.success(msglist);
            }

            // 第2次查询缓存或者数据库，获取每个msgId对应的msg内容
            // TODO 考虑分页，limit
            List<Object> result = redisTemplate.executePipelined((RedisConnection connection) -> {
                for (Object msgId : msgIds) {
                    String key2 = RedisKey.CHAT_SESSION_MSG_ID_MSG + sessionId + Const.SPLIT_C + msgId;
                    connection.get(key2.getBytes());
                }
                return null;
            });

            for (Object obj : result) {
                MsgChat msgChat = JSON.parseObject((String) obj, MsgChat.class);
                msglist.add(msgChat);
            }
            return ResultUtil.success(msglist);
        }
        else { // 7天外查询MongoDB
            // TODO 考虑分页，limit
            Query query = new Query();
            query.addCriteria(Criteria.where("sessionId").is(sessionId).and("msgId").gt(lastMsgId));
            List<MsgChat> msglist = mongoTemplate.find(query, MsgChat.class);
            return ResultUtil.success(msglist);
        }
    }

}
