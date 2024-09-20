package com.hibob.anyim.chat.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.dto.request.*;

import com.hibob.anyim.chat.dto.vo.ChatMsgVO;
import com.hibob.anyim.chat.dto.vo.ChatSessionVO;
import com.hibob.anyim.chat.entity.MsgChat;
import com.hibob.anyim.chat.entity.Session;
import com.hibob.anyim.chat.mapper.SessionMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.session.ReqSession;
import com.hibob.anyim.common.utils.BeanUtil;
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

    private final SessionMapper sessionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final RpcClient rpcClient;

    public ResponseEntity<IMHttpResponse> pullMsg(PullChatMsgReq dto) {
        HashMap<String, Object> resultMap = new HashMap<>();
        String sessionId = dto.getSessionId();
        int pageSize = dto.getPageSize();
        long readMsgId = dto.getReadMsgId();
        long readTime = dto.getReadTime().getTime();
        long currentTime = new Date().getTime();

        // 获取sessionId下面的msgId集合
        String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        long count = redisTemplate.opsForZSet().count(key1, readMsgId + 1, Double.MAX_VALUE);  //由于msg-capacity-in-redis的限制，最多拉取10000条
        if (currentTime - readTime < msgTtlInRedis * 1000) { // 7天内查询Redis
            List<MsgChat> msgList = new ArrayList<>();
            final LinkedHashSet<Object> msgIds;
            if (count > 0) { // 有未读消息，一次查完
                msgIds = (LinkedHashSet)redisTemplate.opsForZSet().rangeByScore(key1, readMsgId + 1, Double.MAX_VALUE);
                int last = (int)msgIds.toArray()[msgIds.size() - 1];
                readMsgId = last;
            }
            else { // 没有未读消息，按pageSize数量查询历史消息（倒序）
                msgIds = (LinkedHashSet)redisTemplate.opsForZSet().reverseRangeByScore(key1, -1, Double.MAX_VALUE, 0, pageSize);
            }

            // 获取每个msgId对应的msg内容
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

            Object[] array = msgList.stream().map(item -> BeanUtil.copyProperties(item, ChatMsgVO.class)).toArray();
            resultMap.put("count", count);
            resultMap.put("lastMsgId", readMsgId);
            resultMap.put("msgList", array);
        }
        else { // 7天外查询MongoDB
            resultMap = count > 0 ? queryMsgFromDbForUnRead(sessionId, readMsgId) : queryMsgFromDbReverse(sessionId, pageSize);

        }

        // 拉取消息后，把session中的已读更新了
        String account = ReqSession.getSession().getAccount();
        LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Session::getAccount, account)
                .eq(Session::getSessionId, sessionId);
        updateWrapper.set(Session::getReadMsgId, resultMap.get("lastMsgId"));
        updateWrapper.set(Session::getReadTime, new Date());
        sessionMapper.update(updateWrapper);

        return ResultUtil.success(resultMap);
    }

    public ResponseEntity<IMHttpResponse> history(ChatHistoryReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String toAccount = dto.getToAccount();
        String sessionId = CommonUtil.combineId(account, toAccount);
        Date startTime = new Date(dto.getStartTime());
        Date endTime = new Date(dto.getEndTime());
        int pageSize = dto.getPageSize();
        long readMsgId = dto.getReadMsgId();

        HashMap<String, Object> resultMap = queryMsgFromDB(sessionId, startTime, endTime, readMsgId, pageSize, false);
        return ResultUtil.success(resultMap);
    }

    public ResponseEntity<IMHttpResponse> sessionList(ChatSessionListReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        LambdaQueryWrapper<Session> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Session::getAccount, account);
        List<Session> sessionList = sessionMapper.selectList(queryWrapper);
        Map<String, ChatSessionVO> voMap = new HashMap<>();
        List<String> toAccountList = new ArrayList<>();
        List<Long> groupIdList = new ArrayList<>();
        for (Session item : sessionList) {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setSessionId(item.getSessionId());
            vo.setSessionType(item.getSessionType());
            vo.setRemoteId(item.getRemoteId());
            if (item.getSessionType() == 2) {  // TODO 要用MsgType里面的枚举，protobuf包要挪到common里面去
                toAccountList.add(item.getRemoteId());
            }
            else if(item.getSessionType() == 3) { // TODO 要用MsgType里面的枚举，protobuf包要挪到common里面去
                groupIdList.add(Long.valueOf(item.getRemoteId()));
            }
            vo.setReadMsgId(item.getReadMsgId());
            vo.setReadTime(item.getReadTime());
            vo.setTop(item.isTop());
            vo.setMuted(item.isMuted());
            vo.setDraft(item.getDraft());
            loadLastMsg(item.getSessionId(), item.getReadMsgId(), vo);
            voMap.put(item.getSessionId(), vo);
        }

        Map<String, Map<String, Object>> usersMap = null;
        Map<Long, Map<String, Object>> groupInfoMap = null;
        if (toAccountList.size() == 0 && groupIdList.size() == 0) {
            return ResultUtil.success(voMap);
        } else if (toAccountList.size() > 0) {
            usersMap = rpcClient.getUserRpcService().queryUserInfoBatch(toAccountList);
        } else if (groupIdList.size() > 0) {
            groupInfoMap = rpcClient.getGroupMngRpcService().queryGroupInfoBatch(groupIdList);
        }

        for (ChatSessionVO vo : voMap.values()) {
            if (vo.getSessionType() == 2) { // TODO 要用MsgType里面的枚举，protobuf包要挪到common里面去
                vo.setObjectInfo(usersMap.get(vo.getRemoteId()));
            }
            else if(vo.getSessionType() == 3) { // TODO 要用MsgType里面的枚举，protobuf包要挪到common里面去
                vo.setObjectInfo(groupInfoMap.get(Long.valueOf(vo.getRemoteId())));
            }
        }

        return ResultUtil.success(voMap);
    }

    public ResponseEntity<IMHttpResponse> updateSession(UpdateSessionReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String sessionId = dto.getSessionId();
        Boolean top = dto.getTop();
        Boolean muted = dto.getMuted();
        String draft = dto.getDraft();
        if (top == null && muted == null && draft == null) {
            return ResultUtil.error(ServiceErrorCode.ERROR_CHAT_UPDATE_SESSION);
        }

        LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Session::getAccount, account)
                .eq(Session::getSessionId, sessionId);
        if (top != null) updateWrapper.set(Session::isTop, top.booleanValue());
        if (muted != null) updateWrapper.set(Session::isMuted, muted.booleanValue());
        if (draft != null) updateWrapper.set(Session::getDraft, draft);
        sessionMapper.update(updateWrapper);

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> querySession(QuerySessionReq dto) {
        return ResultUtil.success(querySession(ReqSession.getSession().getAccount(), dto.getSessionId()));
    }

    public ResponseEntity<IMHttpResponse> createSession(CreateSessionReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String sessionId = dto.getSessionId();
        String remoteId = dto.getRemoteId();
        int sessionType = dto.getSessionType();

        Session session = new Session();
        session.setAccount(account);
        session.setSessionId(sessionId);
        session.setRemoteId(remoteId);
        session.setSessionType(sessionType);
        int insert = sessionMapper.insert(session);
        if (insert > 0) {
            return ResultUtil.success(querySession(account,sessionId));
        }
        else {
            return ResultUtil.error(ServiceErrorCode.ERROR_CHAT_CREATE_SESSION);
        }
    }

    private ChatSessionVO querySession(String account, String sessionId) {
        LambdaQueryWrapper<Session> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Session::getAccount, account).eq(Session::getSessionId, sessionId);
        Session session = sessionMapper.selectOne(queryWrapper);

        ChatSessionVO vo = new ChatSessionVO();
        vo.setSessionId(session.getSessionId());
        vo.setSessionType(session.getSessionType());
        vo.setRemoteId(session.getRemoteId());
        vo.setReadMsgId(session.getReadMsgId());
        vo.setReadTime(session.getReadTime());
        vo.setTop(session.isTop());
        vo.setMuted(session.isMuted());
        vo.setDraft(session.getDraft());
        Map<String, Object> objectInfo = null;
        if (session.getSessionType() == 2) {  // TODO 要用MsgType里面的枚举，protobuf包要挪到common里面去
            objectInfo = rpcClient.getUserRpcService().queryUserInfo(session.getRemoteId());
        }
        else if(session.getSessionType() == 3) { // TODO 要用MsgType里面的枚举，protobuf包要挪到common里面去
            objectInfo = rpcClient.getGroupMngRpcService().queryGroupInfo(Long.parseLong(session.getRemoteId()));
        }
        vo.setObjectInfo(objectInfo);
        loadLastMsg(session.getSessionId(), session.getReadMsgId(), vo);

        return vo;
    }

    private HashMap<String, Object> queryMsgFromDbForUnRead(String sessionId, long readMsgId) {
        return queryMsgFromDB(sessionId, null, null, readMsgId, Integer.MAX_VALUE, false);
    }

    private HashMap<String, Object> queryMsgFromDbReverse(String sessionId, int pageSize) {
        return queryMsgFromDB(sessionId, null, null, -1, pageSize, true);
    }

    private HashMap<String, Object> queryMsgFromDB(String sessionId, Date startTime, Date endTime, long readMsgId, int pageSize, boolean reverse) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sessionId").is(sessionId).and("msgId").gt(readMsgId));
        if (startTime != null && endTime != null) {
            query.addCriteria(Criteria.where("msgTime").gte(startTime).lt(endTime));
        }
        long count = mongoTemplate.count(query, MsgChat.class);

        Sort sort = reverse ? Sort.by(Sort.Order.desc("msgId")) : Sort.by(Sort.Order.asc("msgId"));
        query.with(sort);
        query.limit(pageSize);
        List<MsgChat> msgList = mongoTemplate.find(query, MsgChat.class);
        Object[] array = msgList.stream().map(item -> BeanUtil.copyProperties(item, ChatMsgVO.class)).toArray();
        long lastMsgId = 0;
        if (msgList.size() > 0) {
            lastMsgId = msgList.get(msgList.size() - 1).getMsgId();
        }

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", count);
        resultMap.put("lastMsgId", lastMsgId);
        resultMap.put("msgList", array);
        return resultMap;
    }

    private MsgChat queryMsgFromDbById(String sessionId, long msgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sessionId").is(sessionId).and("msgId").is(msgId));
        List<MsgChat> msgList = mongoTemplate.find(query, MsgChat.class);
        return  msgList.size() > 0 ? msgList.get(0) : null;
    }

    private void loadLastMsg(String sessionId, long readMsgId, ChatSessionVO vo) {
        String key = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        long count = redisTemplate.opsForZSet().count(key, readMsgId + 1, Double.MAX_VALUE);
        long lastMsgId;
        if (count > 0) {
            Set<Object> objects = redisTemplate.opsForZSet().reverseRange(key, 0, 0);//倒序只取第0个元素，即为msgId最大的拿一个（lastMsgId）
            lastMsgId = ((Integer) objects.toArray()[0]).longValue();
        }
        else {
            lastMsgId = readMsgId;
        }

        Object obj = redisTemplate.opsForValue().get(RedisKey.CHAT_SESSION_MSG_ID_MSG + sessionId + Const.SPLIT_C + lastMsgId);
        MsgChat msg;
        if (obj != null) {
            msg = JSON.parseObject((String) obj, MsgChat.class);
        }
        else { // 如果redis查不到，要去mongodb查询
            msg = queryMsgFromDbById(sessionId, lastMsgId);
        }

        if (msg != null) {
            vo.setLastMsgId(lastMsgId);
            vo.setLastMsgContent(msg.getContent());
            vo.setLastMsgTime(msg.getMsgTime());
            vo.setUnreadCount((int) count);
        }
    }

}
