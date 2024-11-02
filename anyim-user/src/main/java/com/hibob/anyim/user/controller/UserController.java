package com.hibob.anyim.user.controller;

import com.hibob.anyim.common.annotation.CommonHeader;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     * TODO 应该不需要这个接口，如果注册的时候账号重复，前端应该按照返回的特定code处理
     * @param dto
     * @return
     */
    @ApiOperation(value = "账号唯一性校验", notes = "用户指定账号时，检查账号字符串是否已经存在")
    @PostMapping("/validateAccount")
    public ResponseEntity<IMHttpResponse> validateAccount(@Valid @RequestBody ValidateAccountReq dto) {
        return userService.validateAccount(dto);
    }

    // TODO 需要验证码机制配合，否则请求容易被ddos攻击
    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<IMHttpResponse> register(@Valid @RequestBody RegisterReq dto) {
        return userService.register(dto);
    }

    @ApiOperation(value = "用户注销", notes = "用户注销")
    @CommonHeader
    @PostMapping("/deregister")
    public ResponseEntity<IMHttpResponse> deregister(@Valid @RequestBody DeregisterReq dto) {
        return userService.deregister(dto);
    }

    // TODO 怎么防ddos攻击？
    // 前端可以限制登录失败三次，增加图形验证。 需要后端怎么配合？
    // 应该是要把验证图片的功能单独拆出来当个服务器，验证成功后在把请求转发给登录服务器
    // 这样，正常登录的请求，直接进登录服务器，不会受到ddos攻击，超过登录失败三次，就会把请求先分流到验证码服务器
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<IMHttpResponse> login(@Valid @RequestBody LoginReq dto) {
        return userService.login(dto);
    }

    @ApiOperation(value = "用户登出", notes = "用户登出")
    @CommonHeader
    @PostMapping("/logout")
    public ResponseEntity<IMHttpResponse> logout(@Valid @RequestBody LogoutReq dto) {
        return userService.logout(dto);
    }

    @ApiOperation(value = "修改密码", notes = "修改密码")
    @CommonHeader
    @PostMapping("/modifyPwd")
    public ResponseEntity<IMHttpResponse> modifyPwd(@Valid @RequestBody ModifyPwdReq dto) {
        return userService.modifyPwd(dto);
    }

    /**
     * 刷新token
     * accessToken都是通过拦截器校验的，refreshToken只有这个场景用，没必要通过拦截器校验
     * @param refreshToken
     * @param dto
     * @return
     */
    @ApiOperation(value = "刷新token", notes = "刷新token")
    @CommonHeader
    @PostMapping("/refreshToken")
    public ResponseEntity<IMHttpResponse> refreshToken(@RequestHeader("refreshToken") String refreshToken, @Valid @RequestBody RefreshTokenReq dto) {
        return userService.refreshToken(refreshToken, dto);
    }

    @ApiOperation(value = "查询自己信息", notes = "查询自己信息")
    @CommonHeader
    @PostMapping("/querySelf")
    public ResponseEntity<IMHttpResponse> querySelf(@Valid @RequestBody QuerySelfReq dto) {
        return userService.querySelf(dto);
    }

    @ApiOperation(value = "修改自己信息", notes = "修改自己信息")
    @CommonHeader
    @PostMapping("/modifySelf")
    public ResponseEntity<IMHttpResponse> modifySelf(@Valid @RequestBody ModifySelfReq dto) {
        return userService.modifySelf(dto);
    }

    @ApiOperation(value = "查询别人信息", notes = "查询别人信息")
    @CommonHeader
    @PostMapping("/query")
    public ResponseEntity<IMHttpResponse> query(@Valid @RequestBody QueryReq dto) {
        return userService.query(dto);
    }

    @ApiOperation(value = "根据昵称找人", notes = "根据昵称找人")
    @CommonHeader
    @PostMapping("/findByNick")
    public ResponseEntity<IMHttpResponse> findByNick(@Valid @RequestBody FindByNickReq dto) {
        return userService.findByNick(dto);
    }
}
