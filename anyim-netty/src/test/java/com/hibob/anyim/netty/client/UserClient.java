package com.hibob.anyim.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.hibob.anyim.common.utils.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Slf4j
@AllArgsConstructor
public class UserClient {

    private String account;
    private String clientId;
    private String headImage;
    private String inviteCode;
    private String nickName;
    private String password;
    private String phoneNum;

    private final RestTemplate restTemplate = new RestTemplate();

    public JSONObject register() throws URISyntaxException {
        String url = "http://localhost:8010/user/register";
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> map = new HashMap<>();
        map.put("account", account);
        map.put("headImage", headImage);
        map.put("inviteCode", inviteCode);
        map.put("nickName", nickName);
        map.put("password", password);
        map.put("phoneNum", phoneNum);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                new URI(url),
                HttpMethod.POST,
                request,
                String.class);
        return JSONObject.parseObject(response.getBody());
    }

    public JSONObject login() throws URISyntaxException {
        String url = "http://localhost:8010/user/login";
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> map = new HashMap<>();
        map.put("account", account);
        map.put("clientId", clientId);
        map.put("password", password);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                new URI(url),
                HttpMethod.POST,
                request,
                String.class);
        return JSONObject.parseObject(response.getBody());
    }

    public JSONObject validateAccount() throws URISyntaxException {
        String url = "http://localhost:8010/user/validateAccount";
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> map = new HashMap<>();
        map.put("account", account);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                new URI(url),
                HttpMethod.POST,
                request,
                String.class);
        return JSONObject.parseObject(response.getBody());
    }

    public JSONObject logout(String accessToken, String signKey) {
        String url = "http://localhost:8010/user/logout";
        HttpHeaders headers = new HttpHeaders();
        String traceId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String sign = JwtUtil.generateSign(signKey, traceId + timestamp);
        headers.add("traceId", traceId);
        headers.add("timestamp", timestamp);
        headers.add("sign", sign);
        headers.add("accessToken", accessToken);
        Map<String, String> map = new HashMap<>();
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class);
        return JSONObject.parseObject(response.getBody());
    }

    public JSONObject deregister(String accessToken, String signKey) throws URISyntaxException {
        String url = "http://localhost:8010/user/deregister";
        HttpHeaders headers = new HttpHeaders();
        String traceId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String sign = JwtUtil.generateSign(signKey, traceId + timestamp);
        headers.add("traceId", traceId);
        headers.add("timestamp", timestamp);
        headers.add("sign", sign);
        headers.add("accessToken", accessToken);
        Map<String, String> map = new HashMap<>();
        HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                new URI(url),
                HttpMethod.POST,
                request,
                String.class);
        return JSONObject.parseObject(response.getBody());
    }

}
