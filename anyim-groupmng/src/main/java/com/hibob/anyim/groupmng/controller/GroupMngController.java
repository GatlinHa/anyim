package com.hibob.anyim.groupmng.controller;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.groupmng.dao.request.CreateGroupReq;
import com.hibob.anyim.groupmng.service.GroupMngService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
@Api(tags = "群组管理相关的接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/groupmng")
public class GroupMngController {
    private final GroupMngService groupMngService;

    @ApiOperation(value = "创建群组", notes = "创建群组")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/createGroup")
    public ResponseEntity<IMHttpResponse> createGroup(@Valid @RequestBody CreateGroupReq dto) {
        return groupMngService.createGroup(dto);
    }
}
