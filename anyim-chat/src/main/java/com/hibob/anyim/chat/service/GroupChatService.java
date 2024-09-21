package com.hibob.anyim.chat.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.dto.request.GroupChatHistoryReq;
import com.hibob.anyim.chat.dto.request.PullGroupChatMsgReq;
import com.hibob.anyim.chat.entity.MsgGroupChat;
import com.hibob.anyim.chat.entity.Session;
import com.hibob.anyim.chat.mapper.SessionMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.session.ReqSession;
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
public class GroupChatService {

    @Value("${custom.msg-ttl-in-redis:604800}")
    private int msgTtlInRedis;

    private final SessionMapper sessionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final RpcClient rpcClient;

    public ResponseEntity<IMHttpResponse> pullMsg(PullGroupChatMsgReq dto) {
        HashMap<String, Object> resultMap = new HashMap<>();
        long groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();
        if (!rpcClient.getGroupMngRpcService().isMemberInGroup(groupId, account)) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_IN_GROUP);
        }

        String sessionId = String.valueOf(groupId);
        int pageSize = dto.getPageSize();
        LambdaQueryWrapper<Session> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Session::getAccount, account).eq(Session::getSessionId, sessionId);
        Session session = sessionMapper.selectOne(queryWrapper);
        long lastMsgId = session.getLastMsgId();
        Date lastMsgTime = session.getLastMsgTime();
        Date currentTime = new Date();

        // 获取sessionId下面的msgId集合
        String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        long count = redisTemplate.opsForZSet().count(key1, lastMsgId + 1, Double.MAX_VALUE);  //由于msg-capacity-in-redis的限制，最多拉取10000条
        if (currentTime.getTime() - lastMsgTime.getTime() < msgTtlInRedis * 1000) { // 7天内查询Redis
            List<MsgGroupChat> msgList = new ArrayList<>();
            if (count > 0) {
                LinkedHashSet<Object> msgIds = (LinkedHashSet)redisTemplate.opsForZSet().rangeByScore(key1, lastMsgId + 1, Double.MAX_VALUE, 0, pageSize);
                int max = (int) msgIds.toArray()[msgIds.size() - 1];
                lastMsgId = max; //lastMsgId更新

                // 查询缓存或者数据库，获取每个msgId对应的msg内容
                List<Object> result = redisTemplate.executePipelined((RedisConnection connection) -> {
                    for (Object msgId : msgIds) {
                        String key2 = RedisKey.CHAT_SESSION_MSG_ID_MSG + sessionId + Const.SPLIT_C + msgId;
                        connection.get(key2.getBytes());
                    }
                    return null;
                });

                for (Object obj : result) {
                    MsgGroupChat msgGroupChat = JSON.parseObject((String) obj, MsgGroupChat.class);
                    msgList.add(msgGroupChat);
                }
            }

            resultMap.put("count", count);
            resultMap.put("lastMsgId", lastMsgId);
            resultMap.put("msgList", msgList);
            return ResultUtil.success(resultMap);
        }
        else { // 7天外查询MongoDB
            resultMap = count > 0 ? queryMsgFromDbForUnRead(sessionId, lastMsgId) : queryMsgFromDbReverse(sessionId, pageSize);
        }

        // 拉取消息后，把session中的已读更新了
        LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Session::getAccount, account)
                .eq(Session::getSessionId, sessionId);
        updateWrapper.set(Session::getLastMsgId, lastMsgId);
        updateWrapper.set(Session::getLastMsgTime, currentTime);
        sessionMapper.update(updateWrapper);

        return ResultUtil.success(resultMap);
    }

    public ResponseEntity<IMHttpResponse> history(GroupChatHistoryReq dto) {
        long groupId = dto.getGroupId();
        String account = ReqSession.getSession().getAccount();
        if (!rpcClient.getGroupMngRpcService().isMemberInGroup(groupId, account)) {
            return ResultUtil.error(ServiceErrorCode.ERROR_GROUP_MNG_NOT_IN_GROUP);
        }

        String sessionId = String.valueOf(groupId);
        Date startTime = new Date(dto.getStartTime());
        Date endTime = new Date(dto.getEndTime());
        int pageSize = dto.getPageSize();

        HashMap<String, Object> resultMap = queryMsgFromDB(sessionId, startTime, endTime, 0, pageSize, false);
        return ResultUtil.success(resultMap);
    }

    private HashMap<String, Object> queryMsgFromDbForUnRead(String sessionId, long lastMsgId) {
        return queryMsgFromDB(sessionId, null, null, lastMsgId, Integer.MAX_VALUE, false);
    }

    private HashMap<String, Object> queryMsgFromDbReverse(String sessionId, int pageSize) {
        return queryMsgFromDB(sessionId, null, null, 0, pageSize, true);
    }


    private HashMap<String, Object> queryMsgFromDB(String sessionId, Date startTime, Date endTime, long lastMsgId, int pageSize, boolean reverse) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sessionId").is(sessionId));
        if (lastMsgId > 0) {
            query.addCriteria(Criteria.where("msgId").gt(lastMsgId));
        }
        if (startTime != null && endTime != null) {
            query.addCriteria(Criteria.where("msgTime").gte(startTime).lt(endTime));
        }
        long count = mongoTemplate.count(query, MsgGroupChat.class);

        Sort sort = reverse ? Sort.by(Sort.Order.desc("msgId")) : Sort.by(Sort.Order.asc("msgId"));
        query.with(sort);
        query.limit(pageSize);
        List<MsgGroupChat> msgList = mongoTemplate.find(query, MsgGroupChat.class);
        if (!msgList.isEmpty()) {
            lastMsgId = msgList.get(msgList.size() - 1).getMsgId(); //lastMsgId更新
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", count);
        resultMap.put("lastMsgId", lastMsgId);
        resultMap.put("msgList", msgList);
        return resultMap;
    }

}
