package com.hibob.anyim.chat.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hibob.anyim.chat.entity.SessionChat;
import com.hibob.anyim.chat.mapper.SessionChatMapper;
import com.hibob.anyim.chat.utils.SnowflakeId;
import com.hibob.anyim.common.rpc.ChatRpcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hibob.anyim.common.utils.CommonUtil.sortId;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class ChatRpcServiceImpl implements ChatRpcService {

    @Value("${custom.ref-msg-id.default:10000}")
    private int refMsgIdDefault;

    private final SessionChatMapper sessionChatMapper;
    private final SnowflakeId snowflakeId;

    @Override
    public int refMsgId(String fromId, String toId) {
        // 通过fromId和toId查询sessionId
        String[] sorted = sortId(fromId, toId);
        SessionChat sessionChat = selectSessionChat(sorted[0], sorted[1]);
        if (sessionChat == null) {
            // 创建session;
            createSessionChat(sorted[0], sorted[1]);
            return refMsgIdDefault;
        }

        return sessionChat.getRefMsgId();
    }

    @Transactional
    public void createSessionChat(String userA, String userB) {
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
