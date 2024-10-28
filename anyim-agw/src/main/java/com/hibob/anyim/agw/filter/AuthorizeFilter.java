package com.hibob.anyim.agw.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hibob.anyim.common.config.JwtProperties;
import com.hibob.anyim.common.constants.Const;
import com.hibob.anyim.common.constants.RedisKey;
import com.hibob.anyim.common.utils.CommonUtil;
import com.hibob.anyim.common.utils.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.hibob.anyim.common.constants.Const.SPLIT_V;

@Slf4j
@Component
@AllArgsConstructor
public class AuthorizeFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final static List<String> ignoreAuthUrls = new ArrayList<>();

    static {
        ignoreAuthUrls.add("/ws");
        ignoreAuthUrls.add("/user/login");
        ignoreAuthUrls.add("/user/register");
        ignoreAuthUrls.add("/user/validateAccount");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("AuthorizeFilter start......path is {}", exchange.getRequest().getPath());
        if (ignoreAuthUrls.contains(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        boolean isRefreshToken = exchange.getRequest().getURI().getPath().equals("/user/refreshToken");
        String token = isRefreshToken ?
                exchange.getRequest().getHeaders().getFirst("refreshToken") :
                exchange.getRequest().getHeaders().getFirst("accessToken");

        if (!StringUtils.hasLength(token)) {
            log.error("Not logged in, url:{}", exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String tokenSecret = isRefreshToken ? jwtProperties.getRefreshTokenSecret() : jwtProperties.getAccessTokenSecret();
        if (!JwtUtil.checkToken(token, tokenSecret)) {
            log.error("The token has expired, isRefreshToken: {}, url:{}", isRefreshToken, exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String account = JwtUtil.getAccount(token);
        String clientId = JwtUtil.getInfo(token);
        String uniqueId = CommonUtil.conUniqueId(account, clientId);
        String key = isRefreshToken ? RedisKey.USER_ACTIVE_TOKEN_REFRESH + uniqueId : RedisKey.USER_ACTIVE_TOKEN + uniqueId;
        JSONObject value = JSON.parseObject((String) redisTemplate.opsForValue().get(key));
        if (!redisTemplate.hasKey(key) || !value.getString("token").equals(token)) {
            log.error("The token has been deleted, isRefreshToken: {}, url:{}", isRefreshToken, exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String singSecret = (String) value.get("secret");
        if (!AuthSign(exchange, uniqueId, singSecret)) {
            return exchange.getResponse().setComplete();
        }

//        ServerHttpRequest newRequest = exchange.getRequest().mutate()
//                .header("agw-account", account)
//                .header("agw-clientId", clientId)
//                .build();
//        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

        return chain.filter(exchange);
    }

    private boolean AuthSign(ServerWebExchange exchange, String uniqueId, String key) {
        String traceId = exchange.getRequest().getHeaders().getFirst("traceId");
        String timestamp = exchange.getRequest().getHeaders().getFirst("timestamp");
        String sign = exchange.getRequest().getHeaders().getFirst("sign");
        if (!StringUtils.hasLength(traceId) || !StringUtils.hasLength(timestamp) || !StringUtils.hasLength(sign)) {
            log.error("The request header information is incomplete, url:{}", exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (Instant.now().getEpochSecond() - Long.parseLong(timestamp) > Const.REQUEST_SIGN_EXPIRE) {
            log.error("Timestamp exceeds the expiration date, url:{}", exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String redisKey = RedisKey.USER_REQ_RECORD + uniqueId + SPLIT_V + traceId;
        if (redisTemplate.hasKey(redisKey)) {
            log.error("Duplicate requests, url:{}", exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String generatedSign = JwtUtil.generateSign(key, traceId + timestamp);
        if (!sign.equals(generatedSign)) {
            log.error("The checksum sign does not match, url:{}", exchange.getRequest().getURI());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        redisTemplate.opsForValue().set(redisKey, "", Duration.ofSeconds(Const.REQUEST_SIGN_EXPIRE));
        return true;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
