package com.ainotes.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Access Token过期时间（毫秒），默认2小时
     */
    @Value("${jwt.expiration:7200000}")
    private Long expiration;

    /**
     * Refresh Token过期时间（毫秒），默认7天
     */
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成密钥
     *
     * @return 密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Access Token
     *
     * @param userId 用户ID
     * @return Token
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "access");
        return createToken(claims, userId.toString(), expiration);
    }

    /**
     * 生成Refresh Token
     *
     * @param userId 用户ID
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, userId.toString(), REFRESH_EXPIRATION);
    }

    /**
     * 创建Token
     *
     * @param claims  声明
     * @param subject 主题
     * @param expiration 过期时间
     * @return Token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("从Token获取用户ID失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token
     *
     * @param token Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token验证失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Token是否为指定类型
     *
     * @param token     Token
     * @param tokenType 期望的Token类型（access/refresh）
     * @return true-有效且类型匹配，false-无效或类型不匹配
     */
    public boolean validateTokenWithType(String token, String tokenType) {
        try {
            Claims claims = parseToken(token);
            String type = claims.get("type", String.class);
            return tokenType.equals(type);
        } catch (Exception e) {
            log.error("Token验证失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析Token
     *
     * @param token Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从已过期的Token中提取用户ID（仅验证签名，忽略过期）
     *
     * @param token Token
     * @return 用户ID，签名无效时返回null
     */
    public Long getUserIdFromExpiredToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", Long.class);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 过期但签名有效，从body中提取userId
            return e.getClaims().get("userId", Long.class);
        } catch (Exception e) {
            log.error("从过期Token获取用户ID失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断Token是否即将过期
     *
     * @param token      Token
     * @param thresholdMs 阈值（毫秒），剩余时间小于此值视为即将过期
     * @return true-即将过期，false-未即将过期
     */
    public boolean isTokenNearExpiry(String token, long thresholdMs) {
        try {
            Claims claims = parseToken(token);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            return remaining < thresholdMs;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取Token过期时间
     *
     * @param token Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("获取Token过期时间失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断Token是否过期
     *
     * @param token Token
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

}
