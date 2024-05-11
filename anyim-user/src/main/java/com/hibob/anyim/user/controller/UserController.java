package com.hibob.anyim.user.controller;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "用户登录，注册和查询相关的接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * 账号唯一性校验，场景是用户注册时，输入用户名后，离开焦点时，检查用户名是否已经存在
     * 因此，不应该校验token
     * TODO 但是，如果没有限制，又有接口被恶意调用，会导致数据库压力过大
     * @param dto
     * @return
     */
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

    /**
     * 用户注销，需要将token加入到黑名单中
     *
     * @param accessToken
     * @param dto
     * @return
     */
    @ApiOperation(value = "用户注销", notes = "用户注销")
    @PostMapping("/deregister")
    public IMHttpResponse deregister(@RequestHeader("accessToken") String accessToken, @Valid @RequestBody DeregisterReq dto) {
        return userService.deregister(accessToken, dto);
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

    /**
     * 刷新token
     * accessToken都是通过拦截器校验的，refreshToken只有这个场景用，没必要通过拦截器校验
     * @param refreshToken
     * @param dto
     * @return
     */
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/refreshToken")
    public IMHttpResponse refreshToken(@RequestHeader("refreshToken") String refreshToken, @Valid @RequestBody RefreshTokenReq dto) {
        return userService.refreshToken(refreshToken, dto);
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
