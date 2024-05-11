package com.hibob.anyim.user.interceptor;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.utils.JwtUtil;
import com.hibob.anyim.user.config.JwtProperties;
import com.hibob.anyim.user.constants.RedisKey;
import com.hibob.anyim.user.enums.ServiceErrorCode;
import com.hibob.anyim.user.exception.ServiceException;
import com.hibob.anyim.user.session.UserSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@AllArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        //从 http 请求头中取出 token
        String token = request.getHeader("accessToken");
        // 校验token非空
        if (!StringUtils.hasLength(token)) {
            log.error("未登陆，url:{}", request.getRequestURI());
            throw new ServiceException(ServiceErrorCode.ERROR_NO_LOGIN);
        }

        //验证 token
        if (!JwtUtil.checkSign(token, jwtProperties.getAccessTokenSecret())) {
            log.error("token已失效，url:{}", request.getRequestURI());
            throw new ServiceException(ServiceErrorCode.ERROR_ACCESS_TOKEN_EXPIRED);
        }

        String account = JwtUtil.getAccount(token);
        String key = RedisKey.USER_ACTIVE_TOKEN + account;
        if (!redisTemplate.hasKey(key) || !redisTemplate.opsForValue().get(key).equals(token)) {
            log.error("token已删除，url:{}", request.getRequestURI());
            throw new ServiceException(ServiceErrorCode.ERROR_ACCESS_TOKEN_DELETED);
        }

        // 存放session
        String strJson = JwtUtil.getInfo(token);
        UserSession userSession = JSON.parseObject(strJson, UserSession.class);
        request.setAttribute("session", userSession);
        return true;
    }
}
