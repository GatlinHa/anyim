package com.hibob.anyim.chat.service;

import com.hibob.anyim.chat.dto.request.UpdateReq;

import com.hibob.anyim.chat.utils.SnowflakeId;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.session.ReqSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RedisTemplate<String, Object> redisTemplate;

    public ResponseEntity<IMHttpResponse> update(UpdateReq dto) {
        ReqSession reqSession = ReqSession.getSession();
        String account = reqSession.getAccount();

        // 1.查询anyim_chat_session_chat，得到单聊的session集合，用请求参数lastMsgId或lastUpdateTime过滤出更新的session
        // 2.查询单聊消息表anyim_chat_msg_chat
        // TODO 注意时间，7天内查询Redis，7天外查询MongoDB


        // 3.查询anyim_groupmng_group表，得到groupid集合
        // 4.查询anyim_chat_session_groupchat表，得到group的session集合，用请求参数lastMsgId或lastUpdateTime过滤出更新的session
        // 5.查询群聊消息表anyim_chat_msg_groupchat
        // TODO 注意时间，7天内查询Redis，7天外查询MongoDB


        return null;
    }

    
}
