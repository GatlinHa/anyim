package com.hibob.anyim.chat.service;

import com.hibob.anyim.chat.dto.request.NewChatReq;

import com.hibob.anyim.chat.utils.SnowflakeId;
import com.hibob.anyim.common.model.IMHttpResponse;
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
    private final SnowflakeId snowflakeId;


    public ResponseEntity<IMHttpResponse> newChat(NewChatReq dto) {
        String toAccountId = dto.getToAccountId();
        long sessionId = snowflakeId.nextId();

        return null;
    }

    
}
