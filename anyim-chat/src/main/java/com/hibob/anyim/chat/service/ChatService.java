package com.hibob.anyim.chat.service;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.chat.dto.request.HistoryReq;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${custom.msg-ttl-in-redis:604800}")
    private int msgTtlInRedis;

    @Value("${custom.msg-read-count:100}")
    private int msgReadCount;

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    public ResponseEntity<IMHttpResponse> pullMsg(PullChatMsgReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String toAccount = dto.getToAccount();
        String sessionId = CommonUtil.combineId(account, toAccount);
        int pageSize = dto.getPageSize() == 0 ? msgReadCount : dto.getPageSize();
        long lastMsgId = dto.getLastMsgId();
        long lastPullTime = dto.getLastPullTime();
        long currentTime = new Date().getTime() / 1000;

        if (currentTime - lastPullTime < msgTtlInRedis) { // 7天内查询Redis
            // 第1次查询缓存，获取sessionId下面的msgId集合
            String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
            long count = redisTemplate.opsForZSet().count(key1, lastMsgId + 1, Double.MAX_VALUE);  //由于msg-capacity-in-redis的限制，最多拉取10000条
            List<MsgChat> msgList = new ArrayList<>();
            if (count > 0) {
                LinkedHashSet<Object> msgIds = (LinkedHashSet)redisTemplate.opsForZSet().rangeByScore(key1, lastMsgId + 1, Double.MAX_VALUE, 0, pageSize);
                int last = (int)msgIds.toArray()[msgIds.size() - 1];
                lastMsgId = last;

                // 第2次查询缓存或者数据库，获取每个msgId对应的msg内容
                List<Object> result = redisTemplate.executePipelined((RedisConnection connection) -> {
                    for (Object msgId : msgIds) {
                        String key2 = RedisKey.CHAT_SESSION_MSG_ID_MSG + sessionId + Const.SPLIT_C + msgId;
                        connection.get(key2.getBytes());
                    }
                    return null;
                });

                for (Object obj : result) {
                    MsgChat msgChat = JSON.parseObject((String) obj, MsgChat.class);
                    msgList.add(msgChat);
                }
            }

            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("count", count);
            resultMap.put("lastMsgId", lastMsgId);
            resultMap.put("msgList", msgList);
            return ResultUtil.success(resultMap);
        }
        else { // 7天外查询MongoDB
            return ResultUtil.success(queryMsgFromDB(sessionId, lastMsgId, pageSize));
        }
    }

    public ResponseEntity<IMHttpResponse> history(HistoryReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String toAccount = dto.getToAccount();
        String sessionId = CommonUtil.combineId(account, toAccount);
        Date startTime = new Date(dto.getStartTime() * 1000);
        Date endTime = new Date(dto.getEndTime() * 1000);
        int pageSize = dto.getPageSize();
        long lastMsgId = dto.getLastMsgId();

        HashMap<String, Object> resultMap = queryMsgFromDB(sessionId, startTime, endTime, lastMsgId, pageSize);
        return ResultUtil.success(resultMap);
    }

    private HashMap<String, Object> queryMsgFromDB(String sessionId, long lastMsgId, int pageSize) {
        return queryMsgFromDB(sessionId, null, null, lastMsgId, pageSize);
    }

    private HashMap<String, Object> queryMsgFromDB(String sessionId, Date startTime, Date endTime, long lastMsgId, int pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sessionId").is(sessionId).and("msgId").gt(lastMsgId));
        if (startTime != null && endTime != null) {
            query.addCriteria(Criteria.where("msgTime").gte(startTime).lt(endTime));
        }
        long count = mongoTemplate.count(query, MsgChat.class);

        query.with(Sort.by(Sort.Order.asc("msgId")));
        query.limit(pageSize);
        List<MsgChat> msgList = mongoTemplate.find(query, MsgChat.class);
        lastMsgId = msgList.get(msgList.size() - 1).getMsgId();
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", count);
        resultMap.put("lastMsgId", lastMsgId);
        resultMap.put("msgList", msgList);
        return resultMap;
    }

}
