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

    @ApiOperation(value = "关闭一个会话记录", notes = "关闭一个会话记录")
    @CommonHeader
    @PostMapping("/closeSession")
    public ResponseEntity<IMHttpResponse> closeSession(@Valid @RequestBody CloseSessionReq dto) {
        return chatService.closeSession(dto);
    }


    @ApiOperation(value = "创建分组", notes = "创建分组")
    @CommonHeader
    @PostMapping("/createPartition")
    public ResponseEntity<IMHttpResponse> createPartition(@Valid @RequestBody PartitionCreateReq dto) {
        return chatService.createPartition(dto);
    }

    @ApiOperation(value = "查询分组", notes = "查询分组")
    @CommonHeader
    @PostMapping("/queryPartition")
    public ResponseEntity<IMHttpResponse> queryPartition(@Valid @RequestBody PartitionQueryReq dto) {
        return chatService.queryPartition(dto);
    }

    @ApiOperation(value = "删除分组", notes = "删除分组")
    @CommonHeader
    @PostMapping("/delPartition")
    public ResponseEntity<IMHttpResponse> delPartition(@Valid @RequestBody PartitionDelReq dto) {
        return chatService.delPartition(dto);
    }

    @ApiOperation(value = "修改分组名字", notes = "修改分组名字")
    @CommonHeader
    @PostMapping("/updatePartition")
    public ResponseEntity<IMHttpResponse> updatePartition(@Valid @RequestBody PartitionUpdateReq dto) {
        return chatService.updatePartition(dto);
    }
}
