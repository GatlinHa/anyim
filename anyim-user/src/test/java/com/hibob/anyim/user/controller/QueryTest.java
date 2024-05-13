package com.hibob.anyim.user.controller;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.client.UserAgent;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.dto.vo.UserVO;
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
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QueryTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static User user01;
    private static User user02;

    @BeforeClass
    public static void beforeClass() {
        log.info("===>正在执行Test，beforeClass");
        user01 = new User();
        user01.setAccount("account_test01");
        user01.setNickName("nick_name_test01");
        user01.setPassword("password_test01");

        user02 = new User();
        user02.setAccount("account_test02");
        user02.setNickName("nick_name_test02");
        user02.setPassword("password_test02");
    }

    /**
     * 1.查询user02 -> 2.注册user01 -> 3.查询user02 -> 4.user01登录 -> 5.查询user02 -> 6.注册user02 -> 7.查询user02（这个才成功） -> 8.注销user02 -> 9.查询user02 -> forceDeleteUser
     * @throws URISyntaxException
     */
    @Test
    public void test01() throws URISyntaxException {
        log.info("===>正在执行Test，Class: [{}]，Method: [{}]", this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);
        UserAgent.forceDeleteUser(testRestTemplate, port, user02);
        // 1.查询user02
        ResponseEntity<IMHttpResponse> res1 = UserAgent.sendRequest(testRestTemplate, port, new HttpHeaders(), BeanUtil.copyProperties(user02, QueryReq.class));
        // 2.注册user01
        ResponseEntity<IMHttpResponse> res2 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, RegisterReq.class));
        // 3.查询user02
        ResponseEntity<IMHttpResponse> res3 = UserAgent.sendRequest(testRestTemplate, port, new HttpHeaders(), BeanUtil.copyProperties(user02, QueryReq.class));
        // 4.user01登录
        ResponseEntity<IMHttpResponse> res4 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, LoginReq.class));
        // 5.查询user02
        ResponseEntity<IMHttpResponse> res5 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res4), BeanUtil.copyProperties(user02, QueryReq.class));
        // 6.注册user02
        ResponseEntity<IMHttpResponse> res6 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user02, RegisterReq.class));
        // 7.查询user02
        ResponseEntity<IMHttpResponse> res7 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res4), BeanUtil.copyProperties(user02, QueryReq.class));
        // 8.注销user02
        UserAgent.forceDeleteUser(testRestTemplate, port, user02);
        // 9.查询user02
        ResponseEntity<IMHttpResponse> res8 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res4), BeanUtil.copyProperties(user02, QueryReq.class));
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);


        assertTrue(res1.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res3.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res5.getBody().getCode() == ServiceErrorCode.ERROR_NO_REGISTER.code());
        assertTrue(JSON.parseObject(JSON.toJSONString(res7.getBody().getData()), UserVO.class).getNickName().equals(user02.getNickName()));
        assertTrue(res8.getBody().getCode() == ServiceErrorCode.ERROR_NO_REGISTER.code());

    }

}
