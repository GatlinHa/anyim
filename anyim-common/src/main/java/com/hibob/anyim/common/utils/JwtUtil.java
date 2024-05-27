package com.hibob.anyim.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public final class JwtUtil {

    private final static String CLAIM_NAME = "info";
    private final static String SPILT = ">>";

    private JwtUtil() {
    }

    /**
     * 生成jwt字符串  JWT(json web token)
     *
     * @param account   用户Account
     * @param info     用户信息
     * @param expireIn 过期时间
     * @param secret   秘钥
     * @return token
     */
    public static String sign(String account, String info, long expireIn, String secret) {
        try {
            account = account + SPILT + UUID.randomUUID();
            Date date = new Date(System.currentTimeMillis() + expireIn * 1000);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String ret = JWT.create()
                    //将account保存到token里面
                    .withAudience(account)
                    //存放自定义数据
                    .withClaim(CLAIM_NAME, info)
                    //过期时间
                    .withExpiresAt(date)
                    .withKeyId("1")
                    //token的密钥
                    .sign(algorithm);
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据token获取Account
     *
     * @param token 登录token
     * @return 用户Account
     */
    public static String getAccount(String token) {
        try {
            String s = JWT.decode(token).getAudience().get(0);
            return s.split(SPILT)[0];
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据token获取用户数据
     *
     * @param token 用户登录token
     * @return 用户数据
     */
    public static String getInfo(String token) {
        try {
            return JWT.decode(token).getClaim(CLAIM_NAME).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 校验token
     *
     * @param token  用户登录token
     * @param secret 秘钥
     * @return true/false
     */
    public static Boolean checkSign(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * 生成请求签名的key
     * @return 请求签名的key
     */
    public static String generateSecretKey() {
        String SecretStr = null;
        try {
            // 使用HMAC-SHA256算法生成一个SecretKey
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGenerator.generateKey();
            // 将SecretKey转换为Base64字符串
            SecretStr = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return SecretStr;
    }

}
