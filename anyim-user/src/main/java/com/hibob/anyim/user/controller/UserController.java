package com.hibob.anyim.user.controller;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.user.dto.request.*;
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
@Api(tags = "用户登录，注册和查询相关的接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "账号唯一性校验", notes = "用户指定账号时，检查账号字符串是否已经存在")
    @PostMapping("/validateAccount")
    public IMHttpResponse validateAccount(@Valid @RequestBody ValidateAccountReq dto) {
        return userService.validateAccount(dto);
    }

    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/register")
    public IMHttpResponse register(@Valid @RequestBody RegisterReq dto) {
        return userService.register(dto);
    }

    @ApiOperation(value = "用户注销", notes = "用户注销")
    @PostMapping("/deregister")
    public IMHttpResponse deregister(@Valid @RequestBody DeregisterReq dto) {
        return userService.deregister(dto);
    }

    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public IMHttpResponse login(@Valid @RequestBody LoginReq dto) {
        return userService.login(dto);
    }

    @ApiOperation(value = "用户登出", notes = "用户登出")
    @PostMapping("/logout")
    public IMHttpResponse logout(@Valid @RequestBody LogoutReq dto) {
        return userService.logout(dto);
    }

    @ApiOperation(value = "修改密码", notes = "修改密码")
    @PostMapping("/modifyPwd")
    public IMHttpResponse modifyPwd(@Valid @RequestBody ModifyPwdReq dto) {
        return userService.modifyPwd(dto);
    }

    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/refreshToken")
    public IMHttpResponse refreshToken(@Valid @RequestBody RefreshTokenReq dto) {
        return userService.refreshToken(dto);
    }

    @ApiOperation(value = "查询自己信息", notes = "查询自己信息")
    @PostMapping("/querySelf")
    public IMHttpResponse querySelf(@Valid @RequestBody QuerySelfReq dto) {
        return userService.querySelf(dto);
    }

    @ApiOperation(value = "修改自己信息", notes = "修改自己信息")
    @PostMapping("/modifySelf")
    public IMHttpResponse modifySelf(@Valid @RequestBody ModifySelfReq dto) {
        return userService.modifySelf(dto);
    }

    @ApiOperation(value = "查询别人信息", notes = "查询别人信息")
    @PostMapping("/query")
    public IMHttpResponse query(@Valid @RequestBody QueryReq dto) {
        return userService.query(dto);
    }

    @ApiOperation(value = "根据昵称找人", notes = "根据昵称找人")
    @PostMapping("/findByNick")
    public IMHttpResponse findByNick(@Valid @RequestBody FindByNickReq dto) {
        return userService.findByNick(dto);
    }

    @ApiOperation(value = "根据账号找人", notes = "根据账号找人")
    @PostMapping("/findByAccount")
    public IMHttpResponse findByAccount(@Valid @RequestBody FindByAccountReq dto) {
        return userService.findByAccount(dto);
    }

}
