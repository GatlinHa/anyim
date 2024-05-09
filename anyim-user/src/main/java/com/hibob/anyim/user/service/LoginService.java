package com.hibob.anyim.user.service;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.ResultUtils;
import com.hibob.anyim.user.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    public IMHttpResponse register(RegisterDTO dto) {
        // TODO accountId不为空，表示用户自己要指定账号，否则生成随机账号，账号全局唯一
        log.info("LoginService--register");
        return ResultUtils.success();
    }

    public IMHttpResponse deregister(DeregisterDTO dto) {
        log.info("LoginService--deregister");
        return ResultUtils.success();
    }

    public IMHttpResponse login(LoginDTO dto) {
        log.info("LoginService--login");
        return ResultUtils.success();
    }

    public IMHttpResponse logout(LogoutDTO dto) {
        log.info("LoginService--logout");
        return ResultUtils.success();
    }

    public IMHttpResponse modifyPwd(ModifyPwdDTO dto) {
        log.info("LoginService--modifyPwd");
        return ResultUtils.success();
    }

    public IMHttpResponse refreshToken(RefreshTokenDTO dto) {
        log.info("LoginService--refreshToken");
        return ResultUtils.success();
    }
}
