package com.hibob.anyim.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class JwtProperties {

    @Value("${jwt.accessToken.expire:1800}")
    private Integer accessTokenExpire;

    @Value("${jwt.accessToken.secret:default_accessToken_secret}")
    private String accessTokenSecret;

    @Value("${jwt.refreshToken.expire:3600}")
    private Integer refreshTokenExpire;

    @Value("${jwt.refreshToken.secret:default_refreshToken_secret}")
    private String refreshTokenSecret;
}
