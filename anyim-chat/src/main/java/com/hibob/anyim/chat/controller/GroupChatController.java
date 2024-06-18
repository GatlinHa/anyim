package com.hibob.anyim.chat.controller;

import com.hibob.anyim.chat.dto.request.GroupChatHistoryReq;
import com.hibob.anyim.chat.dto.request.PullGroupChatMsgReq;
import com.hibob.anyim.chat.service.GroupChatService;
import com.hibob.anyim.common.annotation.CommonHeader;
import com.hibob.anyim.common.model.IMHttpResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Slf4j
@Api(tags = "和群聊相关的REST接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/groupChat")
public class GroupChatController {

    private final GroupChatService groupChatService;

    @ApiOperation(value = "更新群聊的聊天信息", notes = "更新群聊的聊天信息")
    @CommonHeader
    @PostMapping("/pullMsg")
    public ResponseEntity<IMHttpResponse> pullMsg(@Valid @RequestBody PullGroupChatMsgReq dto) {
        return groupChatService.pullMsg(dto);
    }

    @ApiOperation(value = "查询群聊的历史聊天信息", notes = "查询群聊的历史聊天信息")
    @CommonHeader
    @PostMapping("/history")
    public ResponseEntity<IMHttpResponse> history(@Valid @RequestBody GroupChatHistoryReq dto) {
        return groupChatService.history(dto);
    }

}
