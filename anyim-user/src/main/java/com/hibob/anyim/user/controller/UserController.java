package com.hibob.anyim.user.controller;

import com.hibob.anyim.common.annotation.ApiCommonHeader;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Slf4j
@Api(tags = "用户登录，注册和查询相关的接口")
@Validated
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
    @GetMapping("/validateAccount")
    public ResponseEntity<IMHttpResponse> validateAccount(@Validated ValidateAccountReq dto) {
        return userService.validateAccount(dto);
    }

    // TODO 需要验证码机制配合，否则请求容易被ddos攻击
    @ApiOperation(value = "用户注册", notes = "用户注册")
    @PostMapping("/register")
    public ResponseEntity<IMHttpResponse> register(@Valid @RequestBody RegisterReq dto) {
        return userService.register(dto);
    }

    @ApiOperation(value = "用户注销", notes = "用户注销")
    @ApiCommonHeader
    @DeleteMapping("/deregister")
    public ResponseEntity<IMHttpResponse> deregister() {
        return userService.deregister();
    }

    // TODO 怎么防ddos攻击？
    // 前端可以限制登录失败三次，增加图形验证。 需要后端怎么配合？
    // 应该是要把验证图片的功能单独拆出来当个服务器，验证成功后在把请求转发给登录服务器
    // 这样，正常登录的请求，直接进登录服务器，不会受到ddos攻击，超过登录失败三次，就会把请求先分流到验证码服务器
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @PostMapping("/login")
    public ResponseEntity<IMHttpResponse> login(@RequestHeader("clientType") @NotNull(message = "客户端类型不允许为空") @Range(min = 0, max = 10, message = "客户端类型取值范围:0,10") int clientType,
                                                @RequestHeader("clientName") @NotEmpty(message = "客户端名称不允许为空") @Size(max = 255, message = "客户端名称长度不能大于255字符") String clientName,
                                                @RequestHeader("clientVersion") @NotEmpty(message = "客户端版本不允许为空") @Size(max = 255, message = "客户端名称长度不能大于255字符")  String clientVersion,
                                                @Valid @RequestBody LoginReq dto) {
        return userService.login(clientType, clientName, clientVersion, dto);
    }

    @ApiOperation(value = "用户登出", notes = "用户登出")
    @ApiCommonHeader
    @PostMapping("/logout")
    public ResponseEntity<IMHttpResponse> logout() {
        return userService.logout();
    }

    @ApiOperation(value = "修改密码", notes = "修改密码")
    @ApiCommonHeader
    @PostMapping("/modifyPwd")
    public ResponseEntity<IMHttpResponse> modifyPwd(@Valid @RequestBody ModifyPwdReq dto) {
        return userService.modifyPwd(dto);
    }

    /**
     * 刷新token
     * accessToken都是通过拦截器校验的，refreshToken只有这个场景用，没必要通过拦截器校验
     * @param refreshToken
     * @return
     */
    @ApiOperation(value = "刷新token", notes = "刷新token")
    @ApiCommonHeader
    @PostMapping("/refreshToken")
    public ResponseEntity<IMHttpResponse> refreshToken(@RequestHeader("refreshToken") String refreshToken) {
        return userService.refreshToken(refreshToken);
    }

    @ApiOperation(value = "查询自己信息", notes = "查询自己信息")
    @ApiCommonHeader
    @GetMapping("/querySelf")
    public ResponseEntity<IMHttpResponse> querySelf() {
        return userService.querySelf();
    }

    @ApiOperation(value = "修改自己信息", notes = "修改自己信息")
    @ApiCommonHeader
    @PostMapping("/modifySelf")
    public ResponseEntity<IMHttpResponse> modifySelf(@Valid @RequestBody ModifySelfReq dto) {
        return userService.modifySelf(dto);
    }

    @ApiOperation(value = "查询别人信息", notes = "查询别人信息")
    @ApiCommonHeader
    @GetMapping("/query")
    public ResponseEntity<IMHttpResponse> query(@Validated QueryReq dto) {
        return userService.query(dto);
    }

    @ApiOperation(value = "根据昵称找人", notes = "根据昵称找人")
    @ApiCommonHeader
    @GetMapping("/findByNick")
    public ResponseEntity<IMHttpResponse> findByNick(@Validated FindByNickReq dto) {
        return userService.findByNick(dto);
    }
}
