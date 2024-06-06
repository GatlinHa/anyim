package com.hibob.anyim.chat.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.entity.MsgChat;
import com.hibob.anyim.chat.entity.MsgGroupChat;
import com.hibob.anyim.chat.entity.SessionChat;
import com.hibob.anyim.chat.mapper.MsgChatMapper;
import com.hibob.anyim.chat.mapper.MsgGroupChatMapper;
import com.hibob.anyim.chat.mapper.SessionChatMapper;
import com.hibob.anyim.chat.utils.SnowflakeId;
import com.hibob.anyim.common.rpc.ChatRpcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.hibob.anyim.common.utils.CommonUtil.sortId;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class ChatRpcServiceImpl implements ChatRpcService {

    private final SessionChatMapper sessionChatMapper;
    private final MsgChatMapper msgChatMapper;
    private final MsgGroupChatMapper msgGroupChatMapper;

    private final SnowflakeId snowflakeId;

    @Override
    public long refMsgId(String fromId, String toId, int refMsgIdDefault) {
        // 通过fromId和toId查询sessionId
        String[] sorted = sortId(fromId, toId);
        SessionChat sessionChat = selectSessionChat(sorted[0], sorted[1]);
        if (sessionChat == null) {
            // 创建session;
            createSessionChat(sorted[0], sorted[1], refMsgIdDefault);
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
        return selectSessionChat(sorted[0], sorted[1]).getRefMsgId();
    }

    @Override
    public void chatSave(Map<String, Object> msg) {
        //TODO 异步入库，增加回调，回调内回复“已发送”消息

        // 先查询sessionId
        String fromId = (String) msg.get("fromId");
        String toId = (String) msg.get("toId");
        String[] sorted = sortId(fromId, toId);
        SessionChat sessionChat = selectSessionChat(sorted[0], sorted[1]);
        long sessionId = sessionChat.getSessionId();

        MsgChat msgChat = new MsgChat();
        msgChat.setSessionId(sessionId);
        msgChat.setFromId(fromId);
        msgChat.setFromClient((String) msg.get("fromClient"));
        msgChat.setToId(toId);
        msgChat.setMsgId((long) msg.get("msgId"));
        msgChat.setMsgType((int) msg.get("msgType"));
        msgChat.setContent((String) msg.get("content")); //TODO 内容要加密
        msgChatMapper.insert(msgChat);
    }

    @Override
    public void groupChatSave(Map<String, Object> msg) {
        //TODO 异步入库，增加回调，回调内回复“已发送”消息
        MsgGroupChat msgGroupChat = new MsgGroupChat();
        msgGroupChat.setGroupId((long) msg.get("group_id"));
        msgGroupChat.setFromId((String) msg.get("from_id"));
        msgGroupChat.setFromClient((String) msg.get("from_client"));
        msgGroupChat.setMsgId((long) msg.get("msg_id"));
        msgGroupChat.setMsgType((int) msg.get("msgType"));
        msgGroupChat.setContent((String) msg.get("content")); //TODO 内容要加密
        msgGroupChatMapper.insert(msgGroupChat);
    }

    private void createSessionChat(String userA, String userB, int refMsgIdDefault) {
        SessionChat sessionChat = new SessionChat();
        sessionChat.setSessionId(snowflakeId.nextId());
        sessionChat.setUserA(userA);
        sessionChat.setUserB(userB);
        sessionChat.setRefMsgId(refMsgIdDefault);
        sessionChatMapper.insert(sessionChat);
    }

    private SessionChat selectSessionChat(String userA, String userB) {
        LambdaQueryWrapper<SessionChat> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SessionChat::getUserA, userA).
                eq(SessionChat::getUserB, userB);
        List<SessionChat> sessionChat = sessionChatMapper.selectList(queryWrapper);
        if (sessionChat.size() > 0) {
            return sessionChat.get(0);
        }
        else {
            return null;
        }
    }

}
