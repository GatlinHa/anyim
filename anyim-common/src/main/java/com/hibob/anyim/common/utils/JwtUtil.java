package com.hibob.anyim.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
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
    public static String generateToken(String account, String info, long expireIn, String secret) {
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
            log.error("generateToken exception, exception is {}", e.getMessage());
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
            log.error("getAccount exception, exception is {}", e.getMessage());
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
            log.error("getInfo exception, exception is {}", e.getMessage());
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
    public static Boolean checkToken(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("checkToken exception, exception is {}", e.getMessage());
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
            log.error("generateSecretKey exception, exception is {}", e.getMessage());
        }
        return SecretStr;
    }

    /**
     * 对内容生成签名
     * @param key 签名key
     * @param content 内容
     * @return 签名
     */
    public static String generateSign(String key, String content) {
        try{
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(signData));
        }catch (Exception e){
            log.error("generateSign exception, exception is {}", e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(generateSign("ay2ASp0Ar6SXHKvfj6TZQQzfkpc+j6VqwzxrgjCBa74=", "1231716800305"));
    }

}
