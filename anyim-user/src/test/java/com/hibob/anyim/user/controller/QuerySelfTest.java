package com.hibob.anyim.user.controller;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.client.UserAgent;
import com.hibob.anyim.user.dto.request.LoginReq;
import com.hibob.anyim.user.dto.request.LogoutReq;
import com.hibob.anyim.user.dto.request.QuerySelfReq;
import com.hibob.anyim.user.dto.request.RegisterReq;
import com.hibob.anyim.user.dto.vo.UserVO;
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
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuerySelfTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static UserAgent user01;

    @BeforeClass
    public static void beforeClass() {
        log.info("===>正在执行Test，beforeClass");
        user01 = new UserAgent();
        user01.setAccount("account_test01");
        user01.setNickName("nick_name_test01");
        user01.setPassword("password_test01");
        user01.setClientId("client_id_test01");
    }

    /**
     * 查询 -> 注册 -> 查询 -> 登录 -> 查询 -> 登出 -> 查询 -> forceDeleteUser
     * @throws URISyntaxException
     */
    @Test
    public void test01() throws URISyntaxException {
        log.info("===>正在执行Test，Class: [{}]，Method: [{}]", this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);
        ResponseEntity<IMHttpResponse> res1 = UserAgent.sendRequest(testRestTemplate, port, new HttpHeaders(), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        ResponseEntity<IMHttpResponse> res2 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, RegisterReq.class));
        ResponseEntity<IMHttpResponse> res3 = UserAgent.sendRequest(testRestTemplate, port, new HttpHeaders(), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        ResponseEntity<IMHttpResponse> res4 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, LoginReq.class));
        ResponseEntity<IMHttpResponse> res5 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res4),BeanUtil.copyProperties(user01, QuerySelfReq.class));
        ResponseEntity<IMHttpResponse> res6 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res4), BeanUtil.copyProperties(user01, LogoutReq.class));
        ResponseEntity<IMHttpResponse> res7 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res4),BeanUtil.copyProperties(user01, QuerySelfReq.class));
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);

        assertTrue(res1.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res3.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res5.getBody().getCode() == 0);
        assertTrue(JSON.parseObject(JSON.toJSONString(res5.getBody().getData()), UserVO.class).getNickName().equals(user01.getNickName()));
        assertTrue(res7.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

}
