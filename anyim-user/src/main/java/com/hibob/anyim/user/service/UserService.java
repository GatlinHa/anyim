package com.hibob.anyim.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtils;
import com.hibob.anyim.common.utils.ResultUtils;
import com.hibob.anyim.user.dto.*;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.user.enums.ServiceErrorCode;
import com.hibob.anyim.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;

    public IMHttpResponse validateAccount(ValidateAccountDTO dto) {
        log.info("LoginService--validateAccount");
        User user = findByAccount(dto.getAccount());
        if (user != null) {
            return ResultUtils.error(ServiceErrorCode.ERROR_ACCOUNT_EXIST.code(),
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.desc());
        }
        return ResultUtils.success();
    }

    public IMHttpResponse register(RegisterDTO dto) {
        log.info("LoginService--register");
        // 把dto转成User对象
        User user = BeanUtils.copyProperties(dto, User.class);
        if (user == null) {
            return ResultUtils.error(ServiceErrorCode.ERROR_SERVICE_EXCEPTION.code(), ServiceErrorCode.ERROR_SERVICE_EXCEPTION.desc());
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        user.setAccount(uuid);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        this.save(user);
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

    public IMHttpResponse querySelf(QuerySelfDTO dto) {
        log.info("UserService--querySelf");
        return ResultUtils.success();
    }

    public IMHttpResponse modifySelf(ModifySelfDTO dto) {
        log.info("UserService--modifySelf");
        return ResultUtils.success();
    }

    public IMHttpResponse query(QueryDTO dto) {
        log.info("UserService--query");
        return ResultUtils.success();
    }

    public IMHttpResponse findByNick(FindByNickDTO dto) {
        log.info("UserService--findByNick");
        return ResultUtils.success();
    }

    public IMHttpResponse findByAccount(FindByAccountDTO dto) {
        log.info("UserService--findByAccount");
        return ResultUtils.success();
    }

    private User findByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getAccount, account);
        return this.getOne(queryWrapper);
    }
    
}
