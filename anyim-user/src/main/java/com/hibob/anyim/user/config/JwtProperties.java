package com.hibob.anyim.user.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class JwtProperties {

    @Value("${jwt.accessToken.expire}")
    private Integer accessTokenExpire;

    @Value("${jwt.accessToken.secret}")
    private String accessTokenSecret;

    @Value("${jwt.refreshToken.expire}")
    private Integer refreshTokenExpire;

    @Value("${jwt.refreshToken.secret}")
    private String refreshTokenSecret;
}
