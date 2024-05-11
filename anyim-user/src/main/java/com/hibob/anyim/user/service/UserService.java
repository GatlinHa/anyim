package com.hibob.anyim.user.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.common.utils.JwtUtil;
import com.hibob.anyim.common.utils.ResultUtil;
import com.hibob.anyim.user.constants.RedisKey;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.dto.response.LoginRes;
import com.hibob.anyim.user.dto.response.RefreshTokenRes;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.user.enums.ServiceErrorCode;
import com.hibob.anyim.user.mapper.UserMapper;
import com.hibob.anyim.user.session.UserSession;
import com.hibob.anyim.user.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    public IMHttpResponse validateAccount(ValidateAccountReq dto) {
        log.info("LoginService--validateAccount");
        User user = findByAccount(dto.getAccount());
        if (user != null) {
            log.info("account exist");
            return ResultUtil.error(ServiceErrorCode.ERROR_ACCOUNT_EXIST.code(),
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.desc());
        }
        return ResultUtil.success();
    }

    public IMHttpResponse register(RegisterReq dto) {
        log.info("LoginService--register");
        User user = findByAccount(dto.getAccount());
        if (user != null) {
            log.info("account exist");
            return ResultUtil.error(ServiceErrorCode.ERROR_ACCOUNT_EXIST.code(),
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.desc());
        }

        // 把dto转成User对象
        user = BeanUtil.copyProperties(dto, User.class);
        if (user == null) {
            log.error("BeanUtil.copyProperties error");
            return ResultUtil.error(ServiceErrorCode.ERROR_SERVICE_EXCEPTION.code(), ServiceErrorCode.ERROR_SERVICE_EXCEPTION.desc());
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        this.save(user);
        return ResultUtil.success();
    }

    public IMHttpResponse deregister(String accessToken, DeregisterReq dto) {
        log.info("LoginService--deregister");
        UserSession session = UserSession.getSession();
        if (session == null) {
            log.info("no login");
            return ResultUtil.error(ServiceErrorCode.ERROR_NO_LOGIN.code(),
                    ServiceErrorCode.ERROR_NO_LOGIN.desc());
        }

        String account = session.getAccount();
        this.remove(Wrappers.<User>lambdaQuery().eq(User::getAccount, account));

        String key = RedisKey.USER_ACTIVE_TOKEN + account;
        redisTemplate.delete(key);

        return ResultUtil.success();
    }

    public IMHttpResponse login(LoginReq dto) {
        log.info("LoginService--login");
        String account = dto.getAccount();
        String key = RedisKey.USER_ACTIVE_TOKEN + account;
        if (redisTemplate.hasKey(key)) {
            log.info("multi login");
            return ResultUtil.error(ServiceErrorCode.ERROR_MULTI_LOGIN.code(),
                    ServiceErrorCode.ERROR_MULTI_LOGIN.desc());
        }

        User user = findByAccount(account);
        if (user == null) {
            log.info("no register");
            return ResultUtil.error(ServiceErrorCode.ERROR_NO_REGISTER.code(),
                    ServiceErrorCode.ERROR_NO_REGISTER.desc());
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.info("password error");
            return ResultUtil.error(ServiceErrorCode.ERROR_AUTHENTICATION.code(),
                    ServiceErrorCode.ERROR_AUTHENTICATION.desc());
        }

        UserSession session = new UserSession();
        session.setAccount(user.getAccount());
        session.setNickName(user.getNickName());
        String strJson = JSON.toJSONString(session);
        String accessToken = JwtUtil.sign(
                user.getAccount(),
                strJson,
                jwtProperties.getAccessTokenExpire(),
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.sign(
                user.getAccount(),
                strJson,
                jwtProperties.getRefreshTokenExpire(),
                jwtProperties.getRefreshTokenSecret());

        LoginRes res = new LoginRes();
        res.setAccessToken(accessToken);
        res.setAccessTokenExpires(jwtProperties.getAccessTokenExpire());
        res.setRefreshToken(refreshToken);
        res.setRefreshTokenExpires(jwtProperties.getRefreshTokenExpire());

        redisTemplate.opsForValue().set(key, accessToken, Duration.ofSeconds(jwtProperties.getAccessTokenExpire()));

        return ResultUtil.success(res);
    }

    public IMHttpResponse logout(LogoutReq dto) {
        log.info("LoginService--logout");
        return ResultUtil.success();
    }

    public IMHttpResponse modifyPwd(ModifyPwdReq dto) {
        log.info("LoginService--modifyPwd");
        return ResultUtil.success();
    }

    public IMHttpResponse refreshToken(String refreshToken, RefreshTokenReq dto) {
        log.info("LoginService--refreshToken");

        if (!JwtUtil.checkSign(refreshToken, jwtProperties.getRefreshTokenSecret())) {
            log.info("refreshToken error");
            return ResultUtil.error(ServiceErrorCode.ERROR_REFRESH_TOKEN.code(),
                    ServiceErrorCode.ERROR_REFRESH_TOKEN.desc());
        }

        String account = JwtUtil.getAccount(refreshToken);
        String info = JwtUtil.getInfo(refreshToken);
        String accessToken = JwtUtil.sign(
                account,
                info,
                jwtProperties.getAccessTokenExpire(),
                jwtProperties.getAccessTokenSecret());
        String newRefreshToken = JwtUtil.sign(
                account,
                info,
                jwtProperties.getRefreshTokenExpire(),
                jwtProperties.getRefreshTokenSecret());

        RefreshTokenRes res = new RefreshTokenRes();
        res.setAccessToken(accessToken);
        res.setAccessTokenExpires(jwtProperties.getAccessTokenExpire());
        res.setRefreshToken(newRefreshToken);
        res.setRefreshTokenExpires(jwtProperties.getRefreshTokenExpire());

        String key = RedisKey.USER_ACTIVE_TOKEN + account;
        redisTemplate.opsForValue().set(key, accessToken, Duration.ofSeconds(jwtProperties.getAccessTokenExpire()));

        return ResultUtil.success(res);
    }

    public IMHttpResponse querySelf(QuerySelfReq dto) {
        log.info("UserService--querySelf");
        return ResultUtil.success();
    }

    public IMHttpResponse modifySelf(ModifySelfReq dto) {
        log.info("UserService--modifySelf");
        return ResultUtil.success();
    }

    public IMHttpResponse query(QueryReq dto) {
        log.info("UserService--query");
        return ResultUtil.success();
    }

    public IMHttpResponse findByNick(FindByNickReq dto) {
        log.info("UserService--findByNick");
        return ResultUtil.success();
    }

    public IMHttpResponse findByAccount(FindByAccountReq dto) {
        log.info("UserService--findByAccount");
        return ResultUtil.success();
    }

    private User findByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getAccount, account);
        return this.getOne(queryWrapper);
    }
    
}
