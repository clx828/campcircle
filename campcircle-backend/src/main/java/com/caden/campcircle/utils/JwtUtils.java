package com.caden.campcircle.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    // Token expiration time (24 hours)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // Generate a secure key for HS256
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generate JWT token
     *
     * @param userId   user ID
     * @param userRole user role
     * @return JWT token
     */
    public String generateToken(Long userId, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Validate JWT token
     *
     * @param token JWT token
     * @return Claims if valid, null if invalid
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get user ID from token
     *
     * @param token JWT token
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? ((Number) claims.get("userId")).longValue() : null;
    }

    /**
     * Get user role from token
     *
     * @param token JWT token
     * @return user role
     */
    public String getUserRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? (String) claims.get("userRole") : null;
    }
}