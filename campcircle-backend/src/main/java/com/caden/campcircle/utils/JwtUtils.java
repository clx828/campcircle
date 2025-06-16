package com.caden.campcircle.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    // Token过期时间（24小时）
    private static final long EXPIRATION_TIME = 24L * 60 * 60 * 1000 * 30;

    // 从配置文件读取JWT密钥，如果没有配置则使用默认值
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        // 确保密钥长度足够（至少32字符）
        String key = jwtSecret.length() >= 32 ? jwtSecret : jwtSecret + "padding-to-make-key-long-enough";
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID
     * @param userRole 用户角色
     * @return JWT令牌
     */
    public String generateToken(Long userId, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 如果有效返回Claims，无效返回null
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? ((Number) claims.get("userId")).longValue() : null;
    }

    /**
     * 从令牌中获取用户角色
     *
     * @param token JWT令牌
     * @return 用户角色
     */
    public String getUserRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? (String) claims.get("userRole") : null;
    }
}