package com.hibob.anyim.user.controller;

import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.common.utils.BeanUtil;
import com.hibob.anyim.user.client.UserAgent;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.dto.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URISyntaxException;

import static com.hibob.anyim.user.client.UserAgent.getHeaderForAccessToken;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ModifyPwdTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private static UserAgent user01;
    private static UserAgent user01_errorPwd;
    private static UserAgent user01_newPwd;

    @BeforeClass
    public static void beforeClass() {
        log.info("===>正在执行Test，beforeClass");
        user01 = new UserAgent();
        user01.setAccount("account_test01");
        user01.setNickName("nick_name_test01");
        user01.setPassword("password_test01");
        user01.setClientId("client_id_test01");

        user01_errorPwd = BeanUtil.copyProperties(user01, UserAgent.class);
        user01_errorPwd.setPassword("error_password");

        user01_newPwd = BeanUtil.copyProperties(user01, UserAgent.class);
        user01_newPwd.setPassword("new_password");
    }

    /**
     * 注册 -> 登录 -> 修改密码失败 -> 修改密码成功 -> 无法使用查询 -> 使用旧密码登录 -> 使用新密码登录 -> 可以使用查询 -> forceDeleteUser
     * @throws URISyntaxException
     */
    @Test
    public void test01() throws URISyntaxException {
        log.info("===>正在执行Test，Class: [{}]，Method: [{}]", this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[1].getMethodName());
        try {
            UserAgent.forceDeleteUser(testRestTemplate, port, user01_newPwd);
        }
        catch (Exception e) {
            if (e.getMessage().contains("删除用户失败")) {
                UserAgent.forceDeleteUser(testRestTemplate, port, user01);
            }
            log.error("===>删除用户失败");
            assertTrue(false);
        }


        ResponseEntity<IMHttpResponse> res1 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, RegisterReq.class));
        ResponseEntity<IMHttpResponse> res2 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, LoginReq.class));
        ModifyPwdReq modifyPwdReq = new ModifyPwdReq();
        modifyPwdReq.setOldPassword(user01_errorPwd.getPassword());
        modifyPwdReq.setPassword(user01_newPwd.getPassword());
        ResponseEntity<IMHttpResponse> res3 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), modifyPwdReq);
        modifyPwdReq.setOldPassword(user01.getPassword());
        ResponseEntity<IMHttpResponse> res4 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), modifyPwdReq);
        ResponseEntity<IMHttpResponse> res5 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res2), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        ResponseEntity<IMHttpResponse> res6 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01, LoginReq.class));
        ResponseEntity<IMHttpResponse> res7 = UserAgent.sendRequest(testRestTemplate, port, BeanUtil.copyProperties(user01_newPwd, LoginReq.class));
        ResponseEntity<IMHttpResponse> res8 = UserAgent.sendRequest(testRestTemplate, port, getHeaderForAccessToken(res7), BeanUtil.copyProperties(user01, QuerySelfReq.class));
        UserAgent.forceDeleteUser(testRestTemplate, port, user01_newPwd, getHeaderForAccessToken(res7));

        assertTrue(res3.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res4.getBody().getCode() == 0);
        assertTrue(res5.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res6.getStatusCode() == HttpStatus.UNAUTHORIZED);
        assertTrue(res7.getBody().getCode() == 0);
        assertTrue(JSON.parseObject(JSON.toJSONString(res8.getBody().getData()), UserVO.class).getNickName().equals(user01.getNickName()));
    }

}
