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
import com.hibob.anyim.user.dto.vo.TokensVO;
import com.hibob.anyim.user.dto.vo.UserVO;
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
import java.util.Date;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    public IMHttpResponse validateAccount(ValidateAccountReq dto) {
        log.info("LoginService--validateAccount");
        User user = getOneByAccount(dto.getAccount());
        if (user != null) {
            log.info("account exist");
            return ResultUtil.error(ServiceErrorCode.ERROR_ACCOUNT_EXIST.code(),
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.desc());
        }
        return ResultUtil.success();
    }

    public IMHttpResponse register(RegisterReq dto) {
        log.info("LoginService--register");
        User user = getOneByAccount(dto.getAccount());
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
        // TODO 先不校验重复登录，后面多设备场景需要同账号多登录状态
//        if (redisTemplate.hasKey(key)) {
//            log.info("multi login");
//            return ResultUtil.error(ServiceErrorCode.ERROR_MULTI_LOGIN.code(),
//                    ServiceErrorCode.ERROR_MULTI_LOGIN.desc());
//        }

        User user = getOneByAccount(account);
        if (user == null) {
            log.info("no register");
            return ResultUtil.error(ServiceErrorCode.ERROR_NO_REGISTER.code(),
                    ServiceErrorCode.ERROR_NO_REGISTER.desc());
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.info("password error");
            return ResultUtil.error(ServiceErrorCode.ERROR_PASSWORD.code(),
                    ServiceErrorCode.ERROR_PASSWORD.desc());
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

        TokensVO vo = new TokensVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpires(jwtProperties.getAccessTokenExpire());
        vo.setRefreshToken(refreshToken);
        vo.setRefreshTokenExpires(jwtProperties.getRefreshTokenExpire());

        redisTemplate.opsForValue().set(key, accessToken, Duration.ofSeconds(jwtProperties.getAccessTokenExpire()));

        return ResultUtil.success(vo);
    }

    public IMHttpResponse logout(LogoutReq dto) {
        log.info("LoginService--logout");
        UserSession session = UserSession.getSession();
        String account = session.getAccount();
        String key = RedisKey.USER_ACTIVE_TOKEN + account;
        redisTemplate.delete(key);

        return ResultUtil.success();
    }

    public IMHttpResponse modifyPwd(ModifyPwdReq dto) {
        log.info("LoginService--modifyPwd");
        UserSession session = UserSession.getSession();
        String account = session.getAccount();
        User user = getOneByAccount(account);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            log.info("password error");
            return ResultUtil.error(ServiceErrorCode.ERROR_PASSWORD.code(),
                    ServiceErrorCode.ERROR_PASSWORD.desc());
        }
        this.update(Wrappers.<User>lambdaUpdate()
                .eq(User::getAccount, account)
                .set(User::getPassword, passwordEncoder.encode(dto.getPassword()))
                .set(User::getUpdateTime, new Date(System.currentTimeMillis())));

        // 修改密码之后应该设为登出状态
        String key = RedisKey.USER_ACTIVE_TOKEN + account;
        redisTemplate.delete(key);

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

        TokensVO vo = new TokensVO();
        vo.setAccessToken(accessToken);
        vo.setAccessTokenExpires(jwtProperties.getAccessTokenExpire());
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshTokenExpires(jwtProperties.getRefreshTokenExpire());

        String key = RedisKey.USER_ACTIVE_TOKEN + account;
        redisTemplate.opsForValue().set(key, accessToken, Duration.ofSeconds(jwtProperties.getAccessTokenExpire()));

        return ResultUtil.success(vo);
    }

    public IMHttpResponse querySelf(QuerySelfReq dto) {
        log.info("UserService--querySelf");
        UserSession session = UserSession.getSession();
        String account = session.getAccount();
        User user = getOneByAccount(account);
        if (user == null) {
            log.error("user not found");
            return ResultUtil.error(ServiceErrorCode.ERROR_NO_REGISTER.code(),
                    ServiceErrorCode.ERROR_NO_REGISTER.desc());
        }

        // 把User对象转成返回对象
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        if (vo == null) {
            log.error("BeanUtil.copyProperties error");
            return ResultUtil.error(ServiceErrorCode.ERROR_SERVICE_EXCEPTION.code(), ServiceErrorCode.ERROR_SERVICE_EXCEPTION.desc());
        }

        return ResultUtil.success(vo);
    }

    public IMHttpResponse modifySelf(ModifySelfReq dto) {
        log.info("UserService--modifySelf");
        UserSession session = UserSession.getSession();
        String account = session.getAccount();
        User user = getOneByAccount(account);
        if (user == null) {
            log.error("user not found");
            return ResultUtil.error(ServiceErrorCode.ERROR_NO_REGISTER.code(),
                    ServiceErrorCode.ERROR_NO_REGISTER.desc());
        }

        this.update(Wrappers.<User>lambdaUpdate()
                .eq(User::getAccount, account)
                .set(User::getNickName, dto.getNickName())
                .set(User::getHeadImage, dto.getHeadImage())
                .set(User::getHeadImageThumb, dto.getHeadImageThumb())
                .set(User::getSex, dto.getSex())
                .set(User::getLevel, dto.getLevel())
                .set(User::getSignature, dto.getSignature())
                .set(User::getUpdateTime, new Date(System.currentTimeMillis())));

        return ResultUtil.success();
    }

    public IMHttpResponse query(QueryReq dto) {
        log.info("UserService--query");
        User user = getOneByAccount(dto.getAccount());
        if (user == null) {
            log.error("user not found");
            return ResultUtil.error(ServiceErrorCode.ERROR_NO_REGISTER.code(),
                    ServiceErrorCode.ERROR_NO_REGISTER.desc());
        }

        // 把User对象转成返回对象
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        if (vo == null) {
            log.error("BeanUtil.copyProperties error");
            return ResultUtil.error(ServiceErrorCode.ERROR_SERVICE_EXCEPTION.code(), ServiceErrorCode.ERROR_SERVICE_EXCEPTION.desc());
        }

        return ResultUtil.success(vo);
    }

    public IMHttpResponse findByNick(FindByNickReq dto) {
        // TODO 这里要分页查询
        log.info("UserService--findByNick");
        String nickNameKeyWords = dto.getNickNameKeyWords();
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(User::getNickName, nickNameKeyWords);
        List<User> lists = this.list(queryWrapper);
        return ResultUtil.success(lists);
    }

    private User getOneByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(User::getAccount, account);
        return this.getOne(queryWrapper);
    }
    
}
