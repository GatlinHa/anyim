package com.hibob.anyim.user.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.common.utils.JwtUtil;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.common.enums.ServiceErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Data
@EqualsAndHashCode(callSuper=false)
public class UserAgent extends User{

    private String clientId;

    private static final Map<Class, String> uriMap = new HashMap<>();

    static {
        uriMap.put(ValidateAccountReq.class, "/user/validateAccount");
        uriMap.put(RegisterReq.class, "/user/register");
        uriMap.put(DeregisterReq.class, "/user/deregister");
        uriMap.put(LoginReq.class, "/user/login");
        uriMap.put(LogoutReq.class, "/user/logout");
        uriMap.put(ModifyPwdReq.class, "/user/modifyPwd");
        uriMap.put(RefreshTokenReq.class, "/user/refreshToken");
        uriMap.put(QuerySelfReq.class, "/user/querySelf");
        uriMap.put(ModifySelfReq.class, "/user/modifySelf");
        uriMap.put(QueryReq.class, "/user/query");
        uriMap.put(FindByNickReq.class, "/user/findByNick");
    }

    public static ResponseEntity<IMHttpResponse> sendRequest(TestRestTemplate testRestTemplate, int port, Object dto) throws URISyntaxException {
        URI uri = new URI("http://localhost:" + port + uriMap.get(dto.getClass()));
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>(dto),
                IMHttpResponse.class);
        return resEntity;
    }

    public static ResponseEntity<IMHttpResponse> sendRequest(TestRestTemplate testRestTemplate, int port, HttpHeaders headers, Object dto) throws URISyntaxException {
        URI uri = new URI("http://localhost:" + port + uriMap.get(dto.getClass()));
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>(dto, headers),
                IMHttpResponse.class);
        return resEntity;
    }

    public static HttpHeaders getHeaderForAccessToken(ResponseEntity<IMHttpResponse> res) {
        HttpHeaders headers = new HttpHeaders();
        JSONObject data = JSON.parseObject(JSON.toJSONString(res.getBody().getData()));
        String token = data.getJSONObject("accessToken").getString("token");
        String secret = data.getJSONObject("accessToken").getString("secret");
        String traceId = UUID.randomUUID().toString();
        long timestamp = Instant.now().getEpochSecond();
        String sign = JwtUtil.generateSign(secret, traceId + timestamp);
        headers.add("traceId", traceId);
        headers.add("timestamp", String.valueOf(timestamp));
        headers.add("sign", sign);
        headers.add("accessToken", token);
        return headers;
    }

    public static HttpHeaders getHeaderForRefreshToken(ResponseEntity<IMHttpResponse> res) {
        HttpHeaders headers = new HttpHeaders();
        JSONObject data = JSON.parseObject(JSON.toJSONString(res.getBody().getData()));
        String token = data.getJSONObject("refreshToken").getString("token");
        String secret = data.getJSONObject("refreshToken").getString("secret");
        String traceId = UUID.randomUUID().toString();
        long timestamp = Instant.now().getEpochSecond();
        String sign = JwtUtil.generateSign(secret, traceId + timestamp);
        headers.add("traceId", traceId);
        headers.add("timestamp", String.valueOf(timestamp));
        headers.add("sign", sign);
        headers.add("refreshToken", token);
        return headers;
    }

    /**
     * 在未登录状态下，测试前清除账号，校验账号唯一性 -> 存在 -> 登录 -> 注销 -> 校验账号唯一性
     */
    public static void forceDeleteUser(TestRestTemplate testRestTemplate, int port, UserAgent user) throws URISyntaxException {
        ResponseEntity<IMHttpResponse> res1 = sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user, ValidateAccountReq.class));
        if (res1.getBody().getCode() == ServiceErrorCode.ERROR_ACCOUNT_EXIST.code()) {
            ResponseEntity<IMHttpResponse> res2 = sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user, LoginReq.class));
            ResponseEntity<IMHttpResponse> res3 = sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), BeanUtil.copyProperties(user, DeregisterReq.class));
            ResponseEntity<IMHttpResponse> res4 = sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user, ValidateAccountReq.class));
            if (res4.getStatusCode() != HttpStatus.OK || res4.getBody().getCode() == ServiceErrorCode.ERROR_ACCOUNT_EXIST.code()) {
                log.info("===>删除用户[{}]失败！", user.getAccount());
                throw new RuntimeException("删除用户失败");
            }
        }
    }

    /**
     * 在已登录状态下，测试前清除账号，校验账号唯一性 -> 存在 -> 登录 -> 注销 -> 校验账号唯一性
     */
    public static void forceDeleteUser(TestRestTemplate testRestTemplate, int port, UserAgent user, HttpHeaders accessTokenHeaders) throws URISyntaxException {
        ResponseEntity<IMHttpResponse> res1 = sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user, ValidateAccountReq.class));
        if (res1.getBody().getCode() == ServiceErrorCode.ERROR_ACCOUNT_EXIST.code()) {
            ResponseEntity<IMHttpResponse> res2 = sendRequest(testRestTemplate, port, accessTokenHeaders, BeanUtil.copyProperties(user, DeregisterReq.class));
            ResponseEntity<IMHttpResponse> res3 = sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user, ValidateAccountReq.class));
            if (res3.getStatusCode() != HttpStatus.OK || res3.getBody().getCode() == ServiceErrorCode.ERROR_ACCOUNT_EXIST.code()) {
                log.info("===>删除用户[{}]失败！", user.getAccount());
                throw new RuntimeException("删除用户失败");
            }
        }
    }

}
