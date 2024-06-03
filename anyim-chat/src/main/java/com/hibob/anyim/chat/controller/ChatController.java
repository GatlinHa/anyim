package com.hibob.anyim.chat.controller;

import com.hibob.anyim.chat.service.ChatService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(tags = "用户登录，注册和查询相关的接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class ChatController {

    private final ChatService chatService;



//    @ApiOperation(value = "用户登出", notes = "用户登出")
//    @ApiImplicitParams(value = {
//            @ApiImplicitParam(name = "traceId", value = "日志追踪Id", required = true, paramType = "header", dataType = "String"),
//            @ApiImplicitParam(name = "timestamp", value = "时间戳", required = true, paramType = "header", dataType = "String"),
//            @ApiImplicitParam(name = "sign", value = "请求签名", required = true, paramType = "header", dataType = "String"),
//            @ApiImplicitParam(name = "accessToken", value = "accessToken", required = true, paramType = "header", dataType = "String"),
//    })
//    @PostMapping("/logout")
//    public ResponseEntity<IMHttpResponse> logout(@Valid @RequestBody LogoutReq dto) {
//        return userService.logout(dto);
//    }
//

}
