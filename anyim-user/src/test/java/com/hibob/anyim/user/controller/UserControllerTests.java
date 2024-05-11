package com.hibob.anyim.user.controller;


import com.alibaba.fastjson.JSON;
import com.hibob.anyim.common.model.IMHttpResponse;
import com.hibob.anyim.user.dto.request.*;
import com.hibob.anyim.user.dto.vo.TokensVO;
import com.hibob.anyim.user.entity.User;
import com.hibob.anyim.user.enums.ServiceErrorCode;
import org.apache.http.util.Asserts;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;

    private final User user01;
    private final User user02;

    private static TokensVO user01_tokensVO;

    public UserControllerTests() {
        User user01 = new User();
        user01.setAccount("account_test01");
        user01.setNickName("nick_name_test01");
        user01.setPassword("password_test01");

        User user02 = new User();
        user02.setAccount("account_test02");
        user02.setNickName("nick_name_test02");
        user02.setPassword("password_test02");

        this.user01 = user01;
        this.user02 = user02;
    }


    /**
     * 注册测试：注册成功
     * @throws URISyntaxException
     */
    @Test
    public void Test01_Register01() throws URISyntaxException {
        System.out.println("==============>Test01_Register01");
        RegisterReq req = new RegisterReq();
        req.setAccount(user01.getAccount());
        req.setNickName(user01.getNickName());
        req.setPassword(user01.getPassword());
        URI uri = new URI("http://localhost:" + port + "/user/register");
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.postForEntity(uri, req, IMHttpResponse.class);
        IMHttpResponse res = resEntity.getBody();
        assert res.getCode() == 0;
    }

    /**
     * 注册测试：重复注册
     * @throws URISyntaxException
     */
    @Test
    public void Test01_Register02() throws URISyntaxException {
        System.out.println("==============>Test01_Register02");
        RegisterReq req = new RegisterReq();
        req.setAccount(user01.getAccount());
        req.setNickName(user01.getNickName());
        req.setPassword(user01.getPassword());
        URI uri = new URI("http://localhost:" + port + "/user/register");
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.postForEntity(uri, req, IMHttpResponse.class);
        IMHttpResponse res = resEntity.getBody();
        assert res.getCode() == ServiceErrorCode.ERROR_ACCOUNT_EXIST.code();
    }

    /**
     * 测试账号唯一性校验：无重复账号
     * @throws URISyntaxException
     */
    @Test
    public void Test02_ValidateAccount01() throws URISyntaxException {
        System.out.println("==============>Test02_ValidateAccount01");
        ValidateAccountReq req = new ValidateAccountReq();
        req.setAccount(user02.getAccount());
        URI uri = new URI("http://localhost:" + port + "/user/validateAccount");
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.postForEntity(uri, req, IMHttpResponse.class);
        IMHttpResponse res = resEntity.getBody();
        assert res.getCode() == 0;
    }

    /**
     * 测试账号唯一性校验：重复账号
     */
    @Test
    public void Test02_ValidateAccount02() throws URISyntaxException {
        System.out.println("==============>Test02_ValidateAccount02");
        ValidateAccountReq req = new ValidateAccountReq();
        req.setAccount(user01.getAccount());
        URI uri = new URI("http://localhost:" + port + "/user/validateAccount");
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.postForEntity(uri, req, IMHttpResponse.class);
        IMHttpResponse res = resEntity.getBody();
        assert res.getCode() == 201;
    }

    /**
     * 登录测试：账号密码正确
     * @throws URISyntaxException
     */
    @Test
    public void Test03_Login01() throws URISyntaxException {
        System.out.println("==============>Test03_Login01");
        LoginReq req = new LoginReq();
        req.setAccount(user01.getAccount());
        req.setPassword(user01.getPassword());
        URI uri = new URI("http://localhost:" + port + "/user/login");
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.postForEntity(uri, req, IMHttpResponse.class);
        IMHttpResponse res = resEntity.getBody();
        user01_tokensVO = JSON.parseObject(JSON.toJSONString(res.getData()), TokensVO.class);
        assert res.getCode() == 0;
        Asserts.notNull(user01_tokensVO.getAccessToken(), "accessToken");
        Asserts.notNull(user01_tokensVO.getRefreshToken(), "refreshToken");
        Asserts.notNull(user01_tokensVO.getAccessTokenExpires(), "accessTokenExpires");
        Asserts.notNull(user01_tokensVO.getRefreshTokenExpires(), "refreshTokenExpires");
    }

//    @Test
    public void Test03_Logout01() throws URISyntaxException {
        System.out.println("==============>Test03_Logout01");
        LogoutReq req = new LogoutReq();
        HttpHeaders headers = new HttpHeaders();
        headers.add("accessToken", user01_tokensVO.getAccessToken());
        URI uri = new URI("http://localhost:" + port + "/user/logout");
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.postForEntity(uri, req, IMHttpResponse.class);
        IMHttpResponse res = resEntity.getBody();
        assert res.getCode() == 0;
    }

    /**
     * 注册注销：注销成功
     * @throws URISyntaxException
     */
    @Test
    public void Test04_Deregister01() throws URISyntaxException {
        System.out.println("==============>Test05_Deregister01");
        DeregisterReq req = new DeregisterReq();
        HttpHeaders headers = new HttpHeaders();
        headers.add("accessToken", user01_tokensVO.getAccessToken());

        URI uri = new URI("http://localhost:" + port + "/user/deregister");
        ResponseEntity<IMHttpResponse> resEntity = testRestTemplate.exchange(
                uri,
                HttpMethod.POST,
                new HttpEntity<>(req, headers),
                IMHttpResponse.class);
        IMHttpResponse res = resEntity.getBody();
        assert res.getCode() == 0;
    }

}
