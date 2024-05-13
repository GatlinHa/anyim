package com.hibob.anyim.user.controller;

import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.client.UserAgent;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.user.enums.ServiceErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URISyntaxException;

import static com.hibob.anyim.user.client.UserAgent.getHeaderForAccessToken;
import static com.hibob.anyim.user.client.UserAgent.getHeaderForRefreshToken;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RefreshTokenTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static User user01;

    @BeforeClass
    public static void beforeClass() {
        log.info("===>正在执行Test，beforeClass");
        user01 = new User();
        user01.setAccount("account_test01");
        user01.setNickName("nick_name_test01");
        user01.setPassword("password_test01");
    }

    /**
     * 注册 -> 登录 -> 刷新token失败（RefreshToken错误） -> 刷新token -> 用老token查询 -> 用新token查询 -> 再次刷新token -> 用新新token查询 ->  -> forceDeleteUser
     * @throws URISyntaxException
     */
    @Test
    public void test01() throws URISyntaxException, InterruptedException {
        log.info("===>正在执行Test，Class: [{}]，Method: [{}]", this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);

        // 注册
        ResponseEntity<IMHttpResponse> res1 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, RegisterReq.class));
        // 登录
        ResponseEntity<IMHttpResponse> res2 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, LoginReq.class));
        // 刷新token失败（用错误的RefreshToken）
        String refreshTokenError = "refreshTokenError";
        HttpHeaders headers = new HttpHeaders();
        headers.add("refreshToken", refreshTokenError);
        ResponseEntity<IMHttpResponse> res3 = UserAgent.sendRequest(testRestTemplate, port, headers, BeanUtil.copyProperties(user01, RefreshTokenReq.class));
        // 刷新token
        ResponseEntity<IMHttpResponse> res4 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForRefreshToken(res2), BeanUtil.copyProperties(user01, RefreshTokenReq.class));
        // 用老token查询
        ResponseEntity<IMHttpResponse> res5 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        // 用新token查询
        ResponseEntity<IMHttpResponse> res6 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res4), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        // 再次刷新token
        ResponseEntity<IMHttpResponse> res7 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForRefreshToken(res4), BeanUtil.copyProperties(user01, RefreshTokenReq.class));
        // 用新新token查询
        ResponseEntity<IMHttpResponse> res8 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res7), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        // forceDeleteUser
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);

        assertTrue(res3.getBody().getCode() == ServiceErrorCode.ERROR_REFRESH_TOKEN.code());
        assertTrue(res4.getBody().getCode() == 0);
        assertTrue(res5.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res6.getBody().getCode() == 0);
        assertTrue(res7.getBody().getCode() == 0);
        assertTrue(res8.getBody().getCode() == 0);

    }

}
