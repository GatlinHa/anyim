package com.hibob.anyim.user.controller;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.user.dto.*;
import com.hibob.anyim.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@Api(tags = "用户信息处理的相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "查询自己信息", notes = "查询自己信息")
    @PostMapping("/querySelf")
    public IMHttpResponse querySelf(@Valid @RequestBody QuerySelfDTO dto) {
        return userService.querySelf(dto);
    }

    @ApiOperation(value = "修改自己信息", notes = "修改自己信息")
    @PostMapping("/modifySelf")
    public IMHttpResponse modifySelf(@Valid @RequestBody ModifySelfDTO dto) {
        return userService.modifySelf(dto);
    }

    @ApiOperation(value = "查询别人信息", notes = "查询别人信息")
    @PostMapping("/query")
    public IMHttpResponse query(@Valid @RequestBody QueryDTO dto) {
        return userService.query(dto);
    }

    @ApiOperation(value = "根据昵称找人", notes = "根据昵称找人")
    @PostMapping("/findByNick")
    public IMHttpResponse findByNick(@Valid @RequestBody FindByNickDTO dto) {
        return userService.findByNick(dto);
    }

    @ApiOperation(value = "根据账号找人", notes = "根据账号找人")
    @PostMapping("/findById")
    public IMHttpResponse findById(@Valid @RequestBody FindByIdDTO dto) {
        return userService.findById(dto);
    }

}
