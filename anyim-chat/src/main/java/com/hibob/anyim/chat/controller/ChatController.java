package com.hibob.anyim.chat.controller;

import com.hibob.anyim.chat.dto.request.HistoryReq;
import com.hibob.anyim.chat.dto.request.PullChatMsgReq;
import com.hibob.anyim.chat.service.ChatService;
import com.hibob.anyim.common.annotation.CommonHeader;
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
@Api(tags = "和单聊相关的REST接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @ApiOperation(value = "更新单聊的聊天信息", notes = "更新单聊的聊天信息")
    @CommonHeader
    @PostMapping("/pullMsg")
    public ResponseEntity<IMHttpResponse> pullMsg(@Valid @RequestBody PullChatMsgReq dto) {
        return chatService.pullMsg(dto);
    }

    @ApiOperation(value = "查询单聊的历史聊天信息", notes = "查询单聊的历史聊天信息")
    @CommonHeader
    @PostMapping("/history")
    public ResponseEntity<IMHttpResponse> history(@Valid @RequestBody HistoryReq dto) {
        return chatService.history(dto);
    }


}
