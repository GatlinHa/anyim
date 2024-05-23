package com.hibob.anyim.user.controller;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.client.UserAgent;
import com.hibob.anyim.user.dto.request.FindByNickReq;
import com.hibob.anyim.user.dto.request.LoginReq;
import com.hibob.anyim.user.dto.request.RegisterReq;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URISyntaxException;

import static com.hibob.anyim.user.client.UserAgent.getHeaderForAccessToken;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FindByNickTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static UserAgent user01;
    private static UserAgent user02;
    private static UserAgent user03;

    @BeforeClass
    public static void beforeClass() {
        log.info("===>正在执行Test，beforeClass");
        user01 = new UserAgent();
        user01.setAccount("account_test01");
        user01.setNickName("nick_name_test01");
        user01.setPassword("password_test01");
        user01.setClientId("client_id_test01");

        user02 = new UserAgent();
        user02.setAccount("account_test02");
        user02.setNickName("nick_name_test02");
        user02.setPassword("password_test02");
        user02.setClientId("client_id_test02");

        user03 = new UserAgent();
        user03.setAccount("account_test03");
        user03.setNickName("other_name_test03");
        user03.setPassword("password_test03");
        user03.setClientId("client_id_test03");
    }

    /**
     * 1.注册user01 -> 2.user01登录 -> 3.查询（nick_name）-> 4.注册user02 -> 5.查询（nick_name） -> 6.注册user03 -> 7.查询（nick_name） -> 8.查询（other_name） -> forceDeleteUser
     * @throws URISyntaxException
     */
    @Test
    public void test01() throws URISyntaxException {
        log.info("===>正在执行Test，Class: [{}]，Method: [{}]", this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);
        UserAgent.forceDeleteUser(testRestTemplate, port, user02);
        UserAgent.forceDeleteUser(testRestTemplate, port, user03);
        FindByNickReq req1 = new FindByNickReq();
        FindByNickReq req2 = new FindByNickReq();
        req1.setNickNameKeyWords("nick_name");
        req2.setNickNameKeyWords("other_name");
        // 1.注册user01
        ResponseEntity<IMHttpResponse> res1 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, RegisterReq.class));
        // 2.user01登录
        ResponseEntity<IMHttpResponse> res2 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, LoginReq.class));
        // 3.查询（nick_name）
        ResponseEntity<IMHttpResponse> res3 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), req1);
        // 4.注册user02
        ResponseEntity<IMHttpResponse> res4 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user02, RegisterReq.class));
        // 5.查询（nick_name）
        ResponseEntity<IMHttpResponse> res5 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), req1);
        // 6.注册user03
        ResponseEntity<IMHttpResponse> res6 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user03, RegisterReq.class));
        // 7.查询（nick_name）
        ResponseEntity<IMHttpResponse> res7 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), req1);
        // 8.查询（name_nick）
        ResponseEntity<IMHttpResponse> res8 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), req2);
        UserAgent.forceDeleteUser(testRestTemplate, port, user01, getHeaderForAccessToken(res2));
        UserAgent.forceDeleteUser(testRestTemplate, port, user02);
        UserAgent.forceDeleteUser(testRestTemplate, port, user03);

        assertTrue(JSON.parseArray(JSON.toJSONString(res3.getBody().getData())).size() == 1);
        assertTrue(JSON.parseArray(JSON.toJSONString(res5.getBody().getData())).size() == 2);
        assertTrue(JSON.parseArray(JSON.toJSONString(res7.getBody().getData())).size() == 2);
        assertTrue(JSON.parseArray(JSON.toJSONString(res8.getBody().getData())).size() == 1);

    }

}
