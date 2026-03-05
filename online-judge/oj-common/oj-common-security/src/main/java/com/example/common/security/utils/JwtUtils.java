package com.example.common.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Map;

public class JwtUtils {

    /**
     * 生成令牌 - 直接使用字符串的字节数组，避免Base64解码
     *
     * @param claims 数据
     * @param secret 密钥（普通字符串，长度至少64字符）
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret) {
        // 关键修改：使用 getBytes() 而不是直接传字符串
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact();
    }

    /**
     * 从令牌中获取数据
     *
     * @param token 令牌
     * @param secret 密钥
     * @return 数据
     */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}