package com.hibob.anyim.chat.controller;

import com.hibob.anyim.chat.dto.request.*;
import com.hibob.anyim.chat.service.ChatService;
import com.hibob.anyim.common.annotation.CommonHeader;
import com.hibob.anyim.common.model.IMHttpResponse;
import io.swagger.annotations.Api;
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
    public ResponseEntity<IMHttpResponse> history(@Valid @RequestBody ChatHistoryReq dto) {
        return chatService.history(dto);
    }

    @ApiOperation(value = "查询会话记录（单群聊）", notes = "查询单会话记录（单群聊）")
    @CommonHeader
    @PostMapping("/sessionList")
    public ResponseEntity<IMHttpResponse> sessionList(@Valid @RequestBody ChatSessionListReq dto) {
        return chatService.sessionList(dto);
    }

    @ApiOperation(value = "更新会话记录的一些信息", notes = "更新会话记录的一些信息")
    @CommonHeader
    @PostMapping("/updateSession")
    public ResponseEntity<IMHttpResponse> updateSession(@Valid @RequestBody UpdateSessionReq dto) {
        return chatService.updateSession(dto);
    }

    @ApiOperation(value = "查询某个会话记录的信息", notes = "查询某个会话记录的信息")
    @CommonHeader
    @PostMapping("/querySession")
    public ResponseEntity<IMHttpResponse> querySession(@Valid @RequestBody QuerySessionReq dto) {
        return chatService.querySession(dto);
    }

    @ApiOperation(value = "创建一个会话记录", notes = "创建一个会话记录")
    @CommonHeader
    @PostMapping("/createSession")
    public ResponseEntity<IMHttpResponse> createSession(@Valid @RequestBody CreateSessionReq dto) {
        return chatService.createSession(dto);
    }

    @ApiOperation(value = "删除一个会话记录", notes = "删除一个会话记录")
    @CommonHeader
    @PostMapping("/deleteSession")
    public ResponseEntity<IMHttpResponse> deleteSession(@Valid @RequestBody DeleteSessionReq dto) {
        return chatService.deleteSession(dto);
    }
}
