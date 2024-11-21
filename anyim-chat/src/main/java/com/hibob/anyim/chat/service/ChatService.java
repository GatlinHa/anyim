package com.hibob.anyim.chat.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.dto.request.*;

import com.hibob.anyim.chat.dto.vo.ChatMsgVO;
import com.hibob.anyim.chat.dto.vo.ChatSessionVO;
import com.hibob.anyim.chat.dto.vo.PartitionVO;
import com.hibob.anyim.chat.entity.MsgDb;
import com.hibob.anyim.chat.entity.Partition;
import com.hibob.anyim.chat.entity.Session;
import com.hibob.anyim.chat.mapper.PartitionMapper;
import com.hibob.anyim.chat.mapper.SessionMapper;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.protobuf.MsgType;
import com.hibob.anyim.common.rpc.client.RpcClient;
import com.hibob.anyim.common.session.ReqSession;
import com.hibob.anyim.common.utils.BeanUtil;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${custom.msg-ttl-in-redis:604800}")
    private int msgTtlInRedis;

    private final SessionMapper sessionMapper;
    private final PartitionMapper partitionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final RpcClient rpcClient;

    public ResponseEntity<IMHttpResponse> pullMsg(PullChatMsgReq dto) {
        HashMap<String, Object> resultMap = new HashMap<>();
        String sessionId = dto.getSessionId();
        int mode = dto.getMode();
        int pageSize = dto.getPageSize();
        long refMsgId = dto.getRefMsgId();
        Date currentTime = new Date();

        // 获取sessionId下面的msgId集合
        String key1 = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        LinkedHashSet<Object> msgIds = new LinkedHashSet<>();
        switch (mode) {
            case 0:
                // 倒序查最近N条，第一条是msgId最大的
                msgIds = (LinkedHashSet)redisTemplate.opsForZSet().reverseRangeByScore(key1, -1, Double.MAX_VALUE, 0, pageSize);
                break;
            case 1:
                // 倒序查refMsgId之前的N条，第一条是msgId最大的
                msgIds = (LinkedHashSet)redisTemplate.opsForZSet().reverseRangeByScore(key1, -1, refMsgId-1, 0, pageSize);
                break;
//            case 2:
//                // 正序查refMsgId之后的N条，第一条是msgId最小的
//                msgIds = (LinkedHashSet)redisTemplate.opsForZSet().rangeByScore(key1, refMsgId + 1, Double.MAX_VALUE, 0, pageSize);
//                break;
        }

        int count = msgIds.size();
        if (count == 0) {
            resultMap.put("count", 0);
            resultMap.put("firstMsgId", 0);
            resultMap.put("lastMsgId", 0);
            resultMap.put("msgList", null);
            return ResultUtil.success(resultMap);
        }

        List<Long> msgIdInRedis = new ArrayList<>();
        List<Long> msgIdInMongoDb = new ArrayList<>();
        long firstMsgId = Long.MAX_VALUE;
        long lastMsgId = 0;
        for (Object o: msgIds) {
            String[] split = ((String)o).split(Const.SPLIT_V);
            long msgId = Long.parseLong(split[0]);
            long time = Long.parseLong(split[1]);
            if (msgId < firstMsgId) firstMsgId = msgId;
            if (msgId > lastMsgId) lastMsgId = msgId;
            if (currentTime.getTime() - time < msgTtlInRedis * 1000L) {
                msgIdInRedis.add(msgId);
            }
            else {
                msgIdInMongoDb.add(msgId);
            }
        }

        List<MsgDb> msgList = new ArrayList<>();
        if (msgIdInRedis.size() > 0) {
            // 获取每个msgId对应的msg内容
            List<Object> resultRedis = redisTemplate.executePipelined((RedisConnection connection) -> {
                for (Object msgId : msgIdInRedis) {
                    String key2 = RedisKey.CHAT_SESSION_MSG + sessionId + Const.SPLIT_C + msgId;
                    connection.get(key2.getBytes());
                }
                return null;
            });
            msgList = resultRedis.stream().map(obj -> JSON.parseObject((String) obj, MsgDb.class)).collect(Collectors.toList());
        }

        if (msgIdInMongoDb.size() > 0) {
            msgList.addAll(queryMsgFromDbByIn(sessionId, msgIdInMongoDb));
        }

        if (msgList.size() > 0) {
            Object[] array = msgList.stream()
                    .map(item -> BeanUtil.copyProperties(item, ChatMsgVO.class))
                    .sorted((a, b) -> Math.toIntExact(a.getMsgId() - b.getMsgId()))
                    .toArray();
            resultMap.put("count", count);
            resultMap.put("firstMsgId", firstMsgId);
            resultMap.put("lastMsgId", lastMsgId);
            resultMap.put("msgList", array);
            return ResultUtil.success(resultMap);
        }
        else {
            return ResultUtil.error(ServiceErrorCode.ERROR_CHAT_PULL_MSG);
        }

    }

    public ResponseEntity<IMHttpResponse> history(ChatHistoryReq dto) {
        String sessionId = dto.getSessionId();
        Date startTime = new Date(dto.getStartTime());
        Date endTime = new Date(dto.getEndTime());
        int pageSize = dto.getPageSize();

        HashMap<String, Object> resultMap = queryMsgFromDB(sessionId, startTime, endTime, 0, pageSize);
        return ResultUtil.success(resultMap);
    }

    public ResponseEntity<IMHttpResponse> sessionList(ChatSessionListReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        Map<String, ChatSessionVO> voMap = getChatSessionVo(account);
        voMap.putAll(getGroupChatSessionVo(account));
        return ResultUtil.success(voMap);
    }

    public ResponseEntity<IMHttpResponse> updateSession(UpdateSessionReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String sessionId = dto.getSessionId();
        Boolean top = dto.getTop();
        Boolean dnd = dto.getDnd();
        String draft = dto.getDraft(); // 注意，当前端设置draft=""的意思是清空草稿
        String mark = dto.getMark();
        Integer partitionId = dto.getPartitionId();
        if (top == null && dnd == null && draft == null && mark == null && partitionId == null) {
            return ResultUtil.error(ServiceErrorCode.ERROR_CHAT_UPDATE_SESSION);
        }

        LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Session::getAccount, account)
                .eq(Session::getSessionId, sessionId);
        if (top != null) updateWrapper.set(Session::isTop, top);
        if (dnd != null) updateWrapper.set(Session::isDnd, dnd);
        if (draft != null) updateWrapper.set(Session::getDraft, draft);
        if (mark != null) updateWrapper.set(Session::getMark, mark);
        if (partitionId != null) updateWrapper.set(Session::getPartitionId, partitionId.intValue());
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

        LambdaQueryWrapper<Session> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Session::getAccount, account)
                .eq(Session::getSessionId, sessionId);
        // 这里是读多写少场景，优先用主键查询确认接下来是update还是insert，这样更快
        int result;
        if (sessionMapper.selectOne(queryWrapper) != null) {
            LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(Session::getAccount, account)
                    .eq(Session::getSessionId, sessionId)
                    .set(Session::getDelFlag, false);
            result = sessionMapper.update(updateWrapper);
        }
        else {
            Session session = new Session();
            session.setAccount(account);
            session.setSessionId(sessionId);
            session.setRemoteId(remoteId);
            session.setSessionType(sessionType);
            result = sessionMapper.insert(session);
        }

        if (result > 0) {
            return ResultUtil.success(querySession(account, sessionId));
        }
        else {
            return ResultUtil.error(ServiceErrorCode.ERROR_CHAT_CREATE_SESSION);
        }
    }

    public ResponseEntity<IMHttpResponse> deleteSession(DeleteSessionReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();
        String sessionId = dto.getSessionId();

        // 这里采用软删除的方式
        LambdaUpdateWrapper<Session> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Session::getAccount, account)
                .eq(Session::getSessionId, sessionId)
                .set(Session::getDelFlag, true);
        int update = sessionMapper.update(updateWrapper);
        if (update > 0) {
            return ResultUtil.success();
        }
        else {
            return ResultUtil.error(ServiceErrorCode.ERROR_CHAT_DELETE_SESSION);
        }
    }


    public ResponseEntity<IMHttpResponse> createPartition(PartitionCreateReq dto) {
        log.info("UserService::createPartition");
        String account = ReqSession.getSession().getAccount();
        String partitionName = dto.getPartitionName();
        int partitionType = dto.getPartitionType();

        LambdaQueryWrapper<Partition> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Partition::getAccount, account);
        queryWrapper.orderByDesc(Partition::getPartitionId);
        queryWrapper.last("limit 1");
        Partition partitionLast = partitionMapper.selectOne(queryWrapper);
        int newId = 1; //0表示没有分组，1是第1个分组ID
        if (partitionLast != null) {
            newId = partitionLast.getPartitionId() + 1;
        }

        Partition partition = new Partition();
        partition.setAccount(account);
        partition.setPartitionId(newId);
        partition.setPartitionName(partitionName);
        partition.setPartitionType(partitionType);
        int insert = partitionMapper.insert(partition);
        if (insert < 1) {
            log.error("createPartition error");
            return ResultUtil.error(ServiceErrorCode.ERROR_CREATE_PARTITION);
        }

        PartitionVO vo = BeanUtil.copyProperties(partition, PartitionVO.class);
        return ResultUtil.success(vo);
    }

    public ResponseEntity<IMHttpResponse> queryPartition(PartitionQueryReq dto) {
        log.info("UserService::queryPartition");
        String account = ReqSession.getSession().getAccount();
        LambdaQueryWrapper<Partition> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Partition::getAccount, account);
        List<Partition> partitions = partitionMapper.selectList(queryWrapper);
        List<PartitionVO> voList = new ArrayList<>();
        partitions.forEach(item -> {
            PartitionVO vo = BeanUtil.copyProperties(item, PartitionVO.class);
            voList.add(vo);
        });

        return ResultUtil.success(voList);
    }

    @Transactional
    public ResponseEntity<IMHttpResponse> delPartition(PartitionDelReq dto) {
        String account = ReqSession.getSession().getAccount();
        int partitionId = dto.getPartitionId();
        LambdaUpdateWrapper<Partition> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Partition::getAccount, account);
        updateWrapper.eq(Partition::getPartitionId, partitionId);
        int delete = partitionMapper.delete(updateWrapper);
        if (delete < 1) {
            log.error("delPartition error");
            return ResultUtil.error(ServiceErrorCode.ERROR_PARTITION_NO_EXIST);
        }

        // 还要把属于这个分区的session表中的partitionId置为默认0的状态
        LambdaUpdateWrapper<Session> updateSessionWrapper = Wrappers.lambdaUpdate();
        updateSessionWrapper.eq(Session::getAccount, account);
        updateSessionWrapper.eq(Session::getPartitionId, partitionId);
        updateSessionWrapper.set(Session::getPartitionId, 0);
        sessionMapper.update(updateSessionWrapper);
        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> updatePartition(PartitionUpdateReq dto) {
        String account = ReqSession.getSession().getAccount();
        int partitionId = dto.getPartitionId();
        String newPartitionName = dto.getNewPartitionName();
        LambdaUpdateWrapper<Partition> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Partition::getAccount, account);
        updateWrapper.eq(Partition::getPartitionId, partitionId);
        updateWrapper.set(Partition::getPartitionName, newPartitionName);
        int update = partitionMapper.update(updateWrapper);
        if (update < 1) {
            log.error("updatePartition error");
            return ResultUtil.error(ServiceErrorCode.ERROR_PARTITION_NO_EXIST);
        }

        return ResultUtil.success();
    }


    private Map<String, ChatSessionVO> getChatSessionVo(String account) {
        List<Session> sessionListChat = sessionMapper.selectSessionListForChat(account);
        Map<String, ChatSessionVO> voMap = new HashMap<>();
        if (sessionListChat == null || sessionListChat.size() == 0) {
            return  voMap;
        }

        List<String> toAccountList = new ArrayList<>();
        for (Session item : sessionListChat) {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setSessionId(item.getSessionId());
            vo.setSessionType(item.getSessionType());
            vo.setRemoteId(item.getRemoteId());
            vo.setReadMsgId(item.getReadMsgId());
            vo.setReadTime(item.getReadTime());
            vo.setRemoteRead(item.getRemoteRead());
            vo.setTop(item.isTop());
            vo.setDnd(item.isDnd());
            vo.setDraft(item.getDraft());
            vo.setMark(item.getMark());
            vo.setPartitionId(item.getPartitionId());
            loadLastMsg(item.getSessionId(), account, item.getReadMsgId(), vo);
            voMap.put(item.getSessionId(), vo);
            toAccountList.add(item.getRemoteId());
        }
        Map<String, Map<String, Object>> usersMap = rpcClient.getUserRpcService().queryUserInfoBatch(toAccountList);
        for (ChatSessionVO vo : voMap.values()) {
            vo.setObjectInfo(usersMap.get(vo.getRemoteId()));
        }
        return voMap;
    }


    private Map<String, ChatSessionVO> getGroupChatSessionVo(String account) {
        LambdaQueryWrapper<Session> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Session::getAccount, account);
        queryWrapper.eq(Session::getDelFlag, false);
        queryWrapper.eq(Session::getSessionType, MsgType.GROUP_CHAT.getNumber());
        List<Session> sessionListGroupChat = sessionMapper.selectList(queryWrapper);
        Map<String, ChatSessionVO> voMap = new HashMap<>();
        if (sessionListGroupChat == null || sessionListGroupChat.size() == 0) {
            return  voMap;
        }

        List<String> groupIdList = new ArrayList<>();
        for (Session item : sessionListGroupChat) {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setSessionId(item.getSessionId());
            vo.setSessionType(item.getSessionType());
            vo.setRemoteId(item.getRemoteId());
            vo.setReadMsgId(item.getReadMsgId());
            vo.setReadTime(item.getReadTime());
            vo.setTop(item.isTop());
            vo.setDnd(item.isDnd());
            vo.setDraft(item.getDraft());
            vo.setMark(item.getMark());
            vo.setPartitionId(item.getPartitionId());
            loadLastMsg(item.getSessionId(), account, item.getReadMsgId(), vo);
            voMap.put(item.getSessionId(), vo);
            groupIdList.add(item.getRemoteId());
        }
        Map<String, Map<String, Object>> groupInfoMap = rpcClient.getGroupMngRpcService().queryGroupInfoBatch(groupIdList);
        for (ChatSessionVO vo : voMap.values()) {
            vo.setObjectInfo(groupInfoMap.get(vo.getRemoteId()));
        }

        return  voMap;
    }

    private ChatSessionVO querySession(String account, String sessionId) {
        Session session = sessionMapper.selectSession(account, sessionId);
        ChatSessionVO vo = new ChatSessionVO();
        vo.setSessionId(session.getSessionId());
        vo.setSessionType(session.getSessionType());
        vo.setRemoteId(session.getRemoteId());
        vo.setReadMsgId(session.getReadMsgId());
        vo.setRemoteRead(session.getRemoteRead());
        vo.setReadTime(session.getReadTime());
        vo.setTop(session.isTop());
        vo.setDnd(session.isDnd());
        vo.setDraft(session.getDraft());
        vo.setMark(session.getMark());
        vo.setPartitionId(session.getPartitionId());
        Map<String, Object> objectInfo = null;
        if (session.getSessionType() == MsgType.CHAT.getNumber()) {
            objectInfo = rpcClient.getUserRpcService().queryUserInfo(session.getRemoteId());
        }
        else if(session.getSessionType() == MsgType.GROUP_CHAT.getNumber()) {
            objectInfo = rpcClient.getGroupMngRpcService().queryGroupInfo(session.getRemoteId());
        }
        vo.setObjectInfo(objectInfo);
        loadLastMsg(session.getSessionId(), account, session.getReadMsgId(), vo);

        return vo;
    }

    private List<MsgDb> queryMsgFromDbByIn(String sessionId, List<Long> msgIds) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sessionId").is(sessionId).and("msgId").in(msgIds));
        query.with(Sort.by(Sort.Order.asc("msgId")));
        List<MsgDb> msgList = mongoTemplate.find(query, MsgDb.class);
        return msgList;
    }

    private HashMap<String, Object> queryMsgFromDB(String sessionId, Date startTime, Date endTime, long lastMsgId, int pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("sessionId").is(sessionId)
                .and("msgId").gt(lastMsgId)
                .and("msgTime").gte(startTime).lt(endTime));

        long count = mongoTemplate.count(query, MsgDb.class);
        query.with(Sort.by(Sort.Order.asc("msgId")));
        query.limit(pageSize);
        List<MsgDb> msgList = mongoTemplate.find(query, MsgDb.class);
        Object[] array = msgList.stream().map(item -> BeanUtil.copyProperties(item, ChatMsgVO.class)).toArray();
        if (!msgList.isEmpty()) {
            lastMsgId = msgList.get(msgList.size() - 1).getMsgId();  //lastMsgId更新
        }

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", count);
        resultMap.put("lastMsgId", lastMsgId);
        resultMap.put("msgList", array);
        return resultMap;
    }

    private int getUnReadCount(String sessionId, String account, long lastMsgId) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("sessionId").is(sessionId)
                .and("fromId").ne(account)
                .and("msgId").gt(lastMsgId));
        return (int) mongoTemplate.count(query, MsgDb.class);
    }


    private MsgDb queryMsgFromDbById(String sessionId, long msgId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("sessionId").is(sessionId).and("msgId").is(msgId));
        List<MsgDb> msgList = mongoTemplate.find(query, MsgDb.class);
        return  msgList.size() > 0 ? msgList.get(0) : null;
    }

    private void loadLastMsg(String sessionId, String account, long readMsgId , ChatSessionVO vo) {
        // 查最后一条msg
        String key = RedisKey.CHAT_SESSION_MSG_ID + sessionId;
        Set<Object> objects = redisTemplate.opsForZSet().reverseRangeByScore(key, -1, Double.MAX_VALUE, 0, 1);//倒序只取第1个元素，即为msgId最大的那一个（lastMsgId）
        if (objects.size() == 0) {
            vo.setLastMsgId(0);
            vo.setLastMsgContent(null);
            vo.setLastMsgTime(null);
            vo.setUnreadCount(0);
            return;
        }
        String[] split = ((String) objects.toArray()[0]).split(Const.SPLIT_V);
        long msgId = Long.parseLong(split[0]); // 最后一条msg的id
        long time = Long.parseLong(split[1]);  // 最后一条msg的时间
        Date currentTime = new Date();
        MsgDb msg = null;
        if (currentTime.getTime() - time < msgTtlInRedis * 1000L) {
            Object obj = redisTemplate.opsForValue().get(RedisKey.CHAT_SESSION_MSG + sessionId + Const.SPLIT_C + msgId);
            if (obj != null) {
                msg = JSON.parseObject((String) obj, MsgDb.class);
            }
        }
        else {
            msg = queryMsgFromDbById(sessionId, msgId);
        }

        // 查未读的消息数
        int unReadCount = getUnReadCount(sessionId, account, readMsgId);
        if (msg != null) {
            vo.setLastMsgId(msgId);
            vo.setLastMsgContent(msg.getContent());
            vo.setLastMsgTime(msg.getMsgTime());
            vo.setUnreadCount(unReadCount);
        }
    }

}
