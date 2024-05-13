package com.hibob.anyim.user.controller;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.client.UserAgent;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.dto.vo.UserVO;
import com.hibob.anyim.user.entity.User;
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
public class ModifySelfTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static User user01;
    private static User user01_new;

    @BeforeClass
    public static void beforeClass() {
        log.info("===>正在执行Test，beforeClass");
        user01 = new User();
        user01.setAccount("account_test01");
        user01.setNickName("nick_name_test01");
        user01.setPassword("password_test01");

        user01_new = BeanUtil.copyProperties(user01, User.class);
        user01_new.setNickName("nick_name_test01_new");
        user01_new.setHeadImage("head_image_test01_new");
        user01_new.setHeadImageThumb("head_image_thumb_test01_new");
        user01_new.setSex(1);
        user01_new.setLevel(0);
        user01_new.setSignature("signature_test01_new");
    }

    /**
     * 注册 -> 登录 -> 查询self -> 修改self -> 查询self -> forceDeleteUser
     * @throws URISyntaxException
     */
    @Test
    public void test01() throws URISyntaxException {
        log.info("===>正在执行Test，Class: [{}]，Method: [{}]", this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);
        // 注册
        ResponseEntity<IMHttpResponse> res1 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, RegisterReq.class));
        // 登录
        ResponseEntity<IMHttpResponse> res2 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, LoginReq.class));
        // 查询self
        ResponseEntity<IMHttpResponse> res3 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        // 修改self
        ResponseEntity<IMHttpResponse> res4 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), BeanUtil.copyProperties(user01_new, ModifySelfReq.class));
        // 查询self
        ResponseEntity<IMHttpResponse> res5 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        UserAgent.forceDeleteUser(testRestTemplate, port, user01);

        assertTrue(JSON.parseObject(JSON.toJSONString(res3.getBody().getData()), UserVO.class).getNickName().equals(user01.getNickName()));
        assertTrue(JSON.parseObject(JSON.toJSONString(res5.getBody().getData()), UserVO.class).getNickName().equals(user01_new.getNickName()));

    }

}
