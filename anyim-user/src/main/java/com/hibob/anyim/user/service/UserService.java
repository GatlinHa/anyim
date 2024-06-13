package com.hibob.anyim.user.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.common.utils.JwtUtil;
import com.hibob.anyim.common.utils.ResultUtil;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.dto.vo.TokensVO;
import com.hibob.anyim.user.dto.vo.UserVO;
import com.hibob.anyim.user.entity.Client;
import com.hibob.anyim.user.entity.Login;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import com.hibob.anyim.user.mapper.ClientMapper;
import com.hibob.anyim.user.mapper.LoginMapper;
import com.hibob.anyim.user.mapper.UserMapper;
import com.hibob.anyim.common.session.ReqSession;
import com.hibob.anyim.common.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ClientMapper clientMapper;
    private final LoginMapper loginMapper;

    public ResponseEntity<IMHttpResponse> validateAccount(ValidateAccountReq dto) {
        log.info("LoginService::validateAccount");
        User user = getOneByAccount(dto.getAccount());
        if (user != null) {
            log.error("account exist");
            return ResultUtil.error(HttpStatus.OK,
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.code(),
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.desc());
        }
        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> register(RegisterReq dto) {
        log.info("LoginService::register");
        User user = getOneByAccount(dto.getAccount());
        if (user != null) {
            log.error("account exist");
            return ResultUtil.error(HttpStatus.OK,
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.code(),
                    ServiceErrorCode.ERROR_ACCOUNT_EXIST.desc());
        }

        // 把dto转成User对象
        user = BeanUtil.copyProperties(dto, User.class);
        if (user == null) {
            log.error("BeanUtil.copyProperties error");
            return ResultUtil.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        this.save(user);
        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> deregister(DeregisterReq dto) {
        log.info("LoginService::deregister");
        ReqSession session = ReqSession.getSession();
        String account = session.getAccount();
        String clientId = session.getClientId();
        String uniqueId = CommonUtil.conUniqueId(account, clientId);
        this.remove(Wrappers.<User>lambdaQuery().eq(User::getAccount, account));
        deleteClient(account);
        deleteLogin(account);

        redisTemplate.delete(RedisKey.USER_ACTIVE_TOKEN + uniqueId);
        redisTemplate.delete(RedisKey.USER_ACTIVE_TOKEN_REFRESH + uniqueId);

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> login(LoginReq dto) {
        log.info("LoginService::login");
        String account = dto.getAccount();
        String clientId = dto.getClientId();
        String uniqueId = CommonUtil.conUniqueId(account, clientId);
        //支持REST接口重复登录，所以这段代码不启用
//        if (redisTemplate.hasKey(key)) {
//            log.error("Repeated login");
//            return ResultUtil.error(
//                    HttpStatus.FORBIDDEN,
//                    ServiceErrorCode.ERROR_MULTI_LOGIN.code(),
//                    ServiceErrorCode.ERROR_MULTI_LOGIN.desc());
//        }

        User user = getOneByAccount(account);
        if (user == null) {
            log.error("no register");
            return ResultUtil.error(HttpStatus.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.error("password error");
            return ResultUtil.error(HttpStatus.UNAUTHORIZED);
        }

        Client client = getOneByUniqueId(uniqueId);
        if (client == null) {
            log.error("client not found");
            insertClient(dto, uniqueId);
        }
        else {
            updateClient(uniqueId);
        }
        insertLogin(account, uniqueId);

        String accessToken = JwtUtil.generateToken(
                user.getAccount(),
                clientId,
                jwtProperties.getAccessTokenExpire(),
                jwtProperties.getAccessTokenSecret());
        String refreshToken = JwtUtil.generateToken(
                user.getAccount(),
                clientId,
                jwtProperties.getRefreshTokenExpire(),
                jwtProperties.getRefreshTokenSecret());

        TokensVO accessTokenVO = new TokensVO();
        TokensVO refreshTokenVO = new TokensVO();
        accessTokenVO.setToken(accessToken);
        accessTokenVO.setSecret(JwtUtil.generateSecretKey());
        accessTokenVO.setExpire(jwtProperties.getAccessTokenExpire());
        refreshTokenVO.setToken(refreshToken);
        refreshTokenVO.setSecret(JwtUtil.generateSecretKey());
        refreshTokenVO.setExpire(jwtProperties.getRefreshTokenExpire());

        redisTemplate.opsForValue().set(
                RedisKey.USER_ACTIVE_TOKEN + uniqueId,
                JSON.toJSONString(accessTokenVO),
                Duration.ofSeconds(jwtProperties.getAccessTokenExpire()));
        redisTemplate.opsForValue().set(
                RedisKey.USER_ACTIVE_TOKEN_REFRESH + uniqueId,
                JSON.toJSONString(refreshTokenVO),
                Duration.ofSeconds(jwtProperties.getRefreshTokenExpire()));

        HashMap<String, TokensVO> map = new HashMap<>();
        map.put("accessToken", accessTokenVO);
        map.put("refreshToken", refreshTokenVO);
        return ResultUtil.success(map);
    }

    public ResponseEntity<IMHttpResponse> logout(LogoutReq dto) {
        log.info("LoginService::logout");
        ReqSession session = ReqSession.getSession();
        String uniqueId = CommonUtil.conUniqueId(session.getAccount(), session.getClientId());
        redisTemplate.delete(RedisKey.USER_ACTIVE_TOKEN + uniqueId);
        redisTemplate.delete(RedisKey.USER_ACTIVE_TOKEN_REFRESH + uniqueId);
        updateLogin(uniqueId, 0);

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> modifyPwd(ModifyPwdReq dto) {
        log.info("LoginService::modifyPwd");
        ReqSession session = ReqSession.getSession();
        String account = session.getAccount();
        String clientId = session.getClientId();
        String uniqueId = CommonUtil.conUniqueId(account, clientId);
        User user = getOneByAccount(account);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            log.error("password error");
            return ResultUtil.error(HttpStatus.UNAUTHORIZED);
        }
        this.update(Wrappers.<User>lambdaUpdate()
                .eq(User::getAccount, account)
                .set(User::getPassword, passwordEncoder.encode(dto.getPassword()))
                .set(User::getUpdateTime, new Date(System.currentTimeMillis())));

        // 修改密码之后应该设为登出状态
        redisTemplate.delete(RedisKey.USER_ACTIVE_TOKEN + uniqueId);
        redisTemplate.delete(RedisKey.USER_ACTIVE_TOKEN_REFRESH + uniqueId);

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> refreshToken(String refreshToken, RefreshTokenReq dto) {
        log.info("LoginService::refreshToken");
        String account = JwtUtil.getAccount(refreshToken);
        String client = JwtUtil.getInfo(refreshToken);
        String accessToken = JwtUtil.generateToken(
                account,
                client,
                jwtProperties.getAccessTokenExpire(),
                jwtProperties.getAccessTokenSecret());

        TokensVO vo = new TokensVO();
        vo.setToken(accessToken);
        vo.setSecret(JwtUtil.generateSecretKey());
        vo.setExpire(jwtProperties.getAccessTokenExpire());

        String uniqueId = CommonUtil.conUniqueId(account, client);
        String key = RedisKey.USER_ACTIVE_TOKEN + uniqueId;
        redisTemplate.opsForValue().set(key, JSON.toJSONString(vo), Duration.ofSeconds(jwtProperties.getAccessTokenExpire()));
        updateLogin(uniqueId, 1);

        HashMap<String, TokensVO> map = new HashMap<>();
        map.put("accessToken", vo);
        return ResultUtil.success(map);
    }

    public ResponseEntity<IMHttpResponse> querySelf(QuerySelfReq dto) {
        log.info("UserService::querySelf");
        ReqSession session = ReqSession.getSession();
        String account = session.getAccount();
        User user = getOneByAccount(account);
        if (user == null) {
            log.error("user not found");
            return ResultUtil.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 把User对象转成返回对象
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        if (vo == null) {
            log.error("BeanUtil.copyProperties error");
            return ResultUtil.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResultUtil.success(vo);
    }

    public ResponseEntity<IMHttpResponse> modifySelf(ModifySelfReq dto) {
        log.info("UserService::modifySelf");
        ReqSession session = ReqSession.getSession();
        String account = session.getAccount();
        User user = getOneByAccount(account);
        if (user == null) {
            log.error("user not found");
            return ResultUtil.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        this.update(Wrappers.<User>lambdaUpdate()
                .eq(User::getAccount, account)
                .set(User::getNickName, dto.getNickName())
                .set(User::getAvatar, dto.getAvatar())
                .set(User::getAvatarThumb, dto.getAvatarThumb())
                .set(User::getSex, dto.getSex())
                .set(User::getLevel, dto.getLevel())
                .set(User::getSignature, dto.getSignature())
                .set(User::getUpdateTime, new Date(System.currentTimeMillis())));

        return ResultUtil.success();
    }

    public ResponseEntity<IMHttpResponse> query(QueryReq dto) {
        log.info("UserService::query");
        User user = getOneByAccount(dto.getAccount());
        if (user == null) {
            log.error("user not found");
            return ResultUtil.success();
        }

        // 把User对象转成返回对象
        UserVO vo = BeanUtil.copyProperties(user, UserVO.class);
        if (vo == null) {
            log.error("BeanUtil.copyProperties error");
            return ResultUtil.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResultUtil.success(vo);
    }

    public ResponseEntity<IMHttpResponse> findByNick(FindByNickReq dto) {
        // TODO 这里要分页查询
        log.info("UserService::findByNick");
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

    private Client getOneByUniqueId(String uniqueId) {
        LambdaQueryWrapper<Client> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Client::getUniqueId, uniqueId);
        List<Client> clients = clientMapper.selectList(queryWrapper);
        if (clients.size() > 0) {
            return clients.get(0);
        }
        else {
            return null;
        }
    }

    private int insertClient(LoginReq dto, String uniqueId) {
        Client client = BeanUtil.copyProperties(dto, Client.class);
        client.setUniqueId(uniqueId);
        client.setCreatedTime(new Date(System.currentTimeMillis()));
        client.setLastLoginTime(new Date(System.currentTimeMillis()));
        return clientMapper.insert(client);
    }

    private int updateClient(String uniqueId) {
        LambdaUpdateWrapper<Client> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(Client::getLastLoginTime, new Date(System.currentTimeMillis()));
        updateWrapper.eq(Client::getUniqueId, uniqueId);
        return clientMapper.update(updateWrapper);
    }

    private int deleteClient(String account) {
        LambdaUpdateWrapper<Client> deleteWrapper = Wrappers.lambdaUpdate();
        deleteWrapper.eq(Client::getAccount, account);
        return clientMapper.delete(deleteWrapper);
    }

    private int insertLogin(String account, String uniqueId) {
        Login login = new Login();
        login.setAccount(account);
        login.setUniqueId(uniqueId);
        login.setLoginTime(new Date(System.currentTimeMillis()));
        return loginMapper.insert(login);
    }

    private int updateLogin(String uniqueId, int op) {
        LambdaUpdateWrapper<Login> updateWrapper = Wrappers.lambdaUpdate();
        if (op == 0) {
            updateWrapper.set(Login::getLogoutTime, new Date(System.currentTimeMillis()));
        }
        else if (op == 1) {
            updateWrapper.set(Login::getRefreshTime, new Date(System.currentTimeMillis()));
        }
        else {
            return 0;
        }
        updateWrapper.eq(Login::getUniqueId, uniqueId);
        return loginMapper.update(updateWrapper);
    }

    private int deleteLogin(String account) {
        LambdaUpdateWrapper<Login> deleteWrapper = Wrappers.lambdaUpdate();
        deleteWrapper.eq(Login::getAccount, account);
        return loginMapper.delete(deleteWrapper);
    }
    
}
