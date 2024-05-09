package com.hibob.anyim.user.controller;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.user.dto.*;
import com.hibob.anyim.user.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "登录和注册相关的接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class LoginController {

    private final LoginService loginService;

    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/register")
    public IMHttpResponse register(@Valid @RequestBody RegisterDTO dto) {
        return loginService.register(dto);
    }

    @ApiOperation(value = "用户注销", notes = "用户注销")
    @PostMapping("/deregister")
    public IMHttpResponse deregister(@Valid @RequestBody DeregisterDTO dto) {
        return loginService.deregister(dto);
    }

    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public IMHttpResponse login(@Valid @RequestBody LoginDTO dto) {
        return loginService.login(dto);
    }

    @ApiOperation(value = "用户登出", notes = "用户登出")
    @PostMapping("/logout")
    public IMHttpResponse logout(@Valid @RequestBody LogoutDTO dto) {
        return loginService.logout(dto);
    }

    @ApiOperation(value = "修改密码", notes = "修改密码")
    @PostMapping("/modifyPwd")
    public IMHttpResponse modifyPwd(@Valid @RequestBody ModifyPwdDTO dto) {
        return loginService.modifyPwd(dto);
    }

    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/refreshToken")
    public IMHttpResponse refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        return loginService.refreshToken(dto);
    }
}
