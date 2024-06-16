package com.hibob.anyim.groupmng.controller;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.groupmng.dao.request.*;
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

    /**
     * 创建群组
     */
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

    /**
     * 查询群列表
     */
    @ApiOperation(value = "查询群组列表", notes = "查询群组列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/queryGroupList")
    public ResponseEntity<IMHttpResponse> queryGroupList(@Valid @RequestBody QueryGroupListReq dto) {
        return groupMngService.queryGroupList(dto);
    }

    /**
     * 查询群信息
     */
    @ApiOperation(value = "查询群组", notes = "查询群组")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/queryGroupInfo")
    public ResponseEntity<IMHttpResponse> queryGroupInfo(@Valid @RequestBody QueryGroupInfoReq dto) {
        return groupMngService.queryGroupInfo(dto);
    }

    /**
     * 修改群信息
     */
    @ApiOperation(value = "修改群组信息", notes = "修改群组信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/modifyGroup")
    public ResponseEntity<IMHttpResponse> modifyGroup(@Valid @RequestBody ModifyGroupReq dto) {
        return groupMngService.modifyGroup(dto);
    }

    /**
     * 解散群组
     */
    @ApiOperation(value = "解散群组", notes = "解散群组")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/delGroup")
    public ResponseEntity<IMHttpResponse> delGroup(@Valid @RequestBody DelGroupReq dto) {
        return groupMngService.delGroup(dto);
    }

    /**
     * 群组加/减人
     */
    @ApiOperation(value = "群组加/减人", notes = "群组加/减人")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/changeMembers")
    public ResponseEntity<IMHttpResponse> changeMembers(@Valid @RequestBody ChangeMembersReq dto) {
        return groupMngService.changeMembers(dto);
    }

    /**
     * 修改成员级别
     */
    @ApiOperation(value = "群组加/减人", notes = "群组加/减人")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
    })
    @PostMapping("/changeRole")
    public ResponseEntity<IMHttpResponse> changeRole(@Valid @RequestBody ChangeRoleReq dto) {
        return groupMngService.changeRole(dto);
    }

}
