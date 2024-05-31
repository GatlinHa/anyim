package com.hibob.anyim.user.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.common.utils.JwtUtil;
import com.hibob.anyim.user.config.JwtProperties;
import com.hibob.anyim.user.session.UserSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;

import static com.hibob.anyim.common.constants.Const.SPLIT_V;

/**
 * 认证拦截器：1.认证签名，2.认证token
 */
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

        boolean isRefreshToken = request.getRequestURI().equals("/user/refreshToken");
        String token = isRefreshToken ? request.getHeader("refreshToken") : request.getHeader("accessToken");
        if (!StringUtils.hasLength(token)) {
            log.error("未登陆，url:{}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String tokenSecret = isRefreshToken ? jwtProperties.getRefreshTokenSecret() : jwtProperties.getAccessTokenSecret();
        if (!JwtUtil.checkToken(token, tokenSecret)) {
            log.error("token已失效，url:{}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String account = JwtUtil.getAccount(token);
        String clientId = JwtUtil.getInfo(token);
        String uniqueId = CommonUtil.conUniqueId(account, clientId);
        String key = isRefreshToken ? RedisKey.USER_ACTIVE_TOKEN_REFRESH + uniqueId : RedisKey.USER_ACTIVE_TOKEN + uniqueId;
        JSONObject value = JSON.parseObject((String) redisTemplate.opsForValue().get(key));
        if (!redisTemplate.hasKey(key) || !value.getString("token").equals(token)) {
            log.error("token已删除，url:{}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        String singSecret = (String) value.get("secret");
        if (!AuthSign(request, response, uniqueId, singSecret)) {
            return false;
        }

        // 存放session
        UserSession userSession = new UserSession();
        userSession.setAccount(account);
        userSession.setClientId(clientId);
        request.setAttribute("session", userSession);
        return true;
    }

    private boolean AuthSign(HttpServletRequest request, HttpServletResponse response, String uniqueId, String key) {
        String traceId = request.getHeader("traceId");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        if (!StringUtils.hasLength(traceId) || !StringUtils.hasLength(timestamp) || !StringUtils.hasLength(sign)) {
            log.error("请求头信息不全，url:{}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        if (Instant.now().getEpochSecond() - Long.parseLong(timestamp) > Const.REQUEST_SIGN_EXPIRE) {
            log.error("timestamp超过有效期，url:{}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String redisKey = RedisKey.USER_REQ_RECORD + uniqueId + SPLIT_V + traceId;
        if (redisTemplate.hasKey(redisKey)) {
            log.error("重复的请求，url:{}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        String generatedSign = JwtUtil.generateSign(key, traceId + timestamp);
        if (!sign.equals(generatedSign)) {
            log.error("校验sign不匹配，url:{}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        redisTemplate.opsForValue().set(redisKey, "", Duration.ofSeconds(Const.REQUEST_SIGN_EXPIRE));
        return true;
    }

}
