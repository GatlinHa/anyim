package com.hibob.anyim.user.client;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.dto.vo.TokensVO;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.user.enums.ServiceErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserAgent {

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
        TokensVO token = JSON.parseObject(JSON.toJSONString(res.getBody().getData()), TokensVO.class);
        headers.add("accessToken", token.getAccessToken());
        return headers;
    }

    public static HttpHeaders getHeaderForRefreshToken(ResponseEntity<IMHttpResponse> res) {
        HttpHeaders headers = new HttpHeaders();
        TokensVO token = JSON.parseObject(JSON.toJSONString(res.getBody().getData()), TokensVO.class);
        headers.add("refreshToken", token.getRefreshToken());
        return headers;
    }

    /**
     * 测试前清除账号，校验账号唯一性 -> 存在 -> 登录 -> 注销 -> 校验账号唯一性
     */
    public static void forceDeleteUser(TestRestTemplate testRestTemplate, int port, User user) throws URISyntaxException {
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

}
