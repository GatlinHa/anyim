package com.hibob.anyim.chat.controller;

import com.hibob.anyim.chat.dto.request.NewChatReq;
import com.hibob.anyim.chat.service.ChatService;
import com.hibob.anyim.common.model.IMHttpResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Slf4j
@Api(tags = "和聊天相关的REST接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;


    @ApiOperation(value = "创建单聊", notes = "创建单聊")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/newChat")
    public ResponseEntity<IMHttpResponse> newChat(@Valid @RequestBody NewChatReq dto) {
        return chatService.newChat(dto);
    }


}
