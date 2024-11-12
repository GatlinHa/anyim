package com.hibob.anyim.groupmng.controller;

import com.hibob.anyim.common.annotation.CommonHeader;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.groupmng.dao.request.*;
import com.hibob.anyim.groupmng.service.GroupMngService;
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
    @CommonHeader
    @PostMapping("/createGroup")
    public ResponseEntity<IMHttpResponse> createGroup(@Valid @RequestBody CreateGroupReq dto) {
        return groupMngService.createGroup(dto);
    }

    /**
     * 查询群列表
     */
    @ApiOperation(value = "查询群组列表", notes = "查询群组列表")
    @CommonHeader
    @PostMapping("/queryGroupList")
    public ResponseEntity<IMHttpResponse> queryGroupList(@Valid @RequestBody QueryGroupListReq dto) {
        return groupMngService.queryGroupList(dto);
    }

    /**
     * 查询群信息
     */
    @ApiOperation(value = "查询群组", notes = "查询群组")
    @CommonHeader
    @PostMapping("/queryGroupInfo")
    public ResponseEntity<IMHttpResponse> queryGroupInfo(@Valid @RequestBody QueryGroupInfoReq dto) {
        return groupMngService.queryGroupInfo(dto);
    }

    /**
     * 根据成员群昵称或账号搜索群id
     */
    @ApiOperation(value = "根据成员群昵称或账号搜索群id", notes = "根据成员群昵称或账号搜索群id")
    @CommonHeader
    @PostMapping("/searchGroupByMember")
    public ResponseEntity<IMHttpResponse> searchGroupByMember(@Valid @RequestBody SearchGroupByMemberReq dto) {
        return groupMngService.searchGroupByMember(dto);
    }

    /**
     * 修改群信息
     */
    @ApiOperation(value = "修改群组信息", notes = "修改群组信息")
    @CommonHeader
    @PostMapping("/modifyGroup")
    public ResponseEntity<IMHttpResponse> modifyGroup(@Valid @RequestBody ModifyGroupReq dto) {
        return groupMngService.modifyGroup(dto);
    }

    /**
     * 解散群组
     */
    @ApiOperation(value = "解散群组", notes = "解散群组")
    @CommonHeader
    @PostMapping("/delGroup")
    public ResponseEntity<IMHttpResponse> delGroup(@Valid @RequestBody DelGroupReq dto) {
        return groupMngService.delGroup(dto);
    }

    /**
     * 群组加/减人
     */
    @ApiOperation(value = "群组加人", notes = "群组加人")
    @CommonHeader
    @PostMapping("/addMembers")
    public ResponseEntity<IMHttpResponse> addMembers(@Valid @RequestBody AddMembersReq dto) {
        return groupMngService.addMembers(dto);
    }

    /**
     * 群组减人
     */
    @ApiOperation(value = "群组减人", notes = "群组减人")
    @CommonHeader
    @PostMapping("/delMembers")
    public ResponseEntity<IMHttpResponse> delMembers(@Valid @RequestBody DelMembersReq dto) {
        return groupMngService.delMembers(dto);
    }

    /**
     * 修改成员级别
     */
    @ApiOperation(value = "修改成员角色（非群主转让）", notes = "修改成员角色（非群主转让）")
    @CommonHeader
    @PostMapping("/changeRole")
    public ResponseEntity<IMHttpResponse> changeRole(@Valid @RequestBody ChangeRoleReq dto) {
        return groupMngService.changeRole(dto);
    }

    /**
     * 群主转让
     */
    @ApiOperation(value = "群主转让", notes = "群主转让")
    @CommonHeader
    @PostMapping("/ownerTransfer")
    public ResponseEntity<IMHttpResponse> ownerTransfer(@Valid @RequestBody OwnerTransferReq dto) {
        return groupMngService.ownerTransfer(dto);
    }

}
