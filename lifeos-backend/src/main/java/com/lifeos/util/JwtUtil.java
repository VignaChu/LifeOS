package com.lifeos.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    // JWT密钥，生产环境应该使用更复杂的密钥并从配置文件读取
    private static final String SECRET = "LifeOSAdminSecretKey2024LifeOSAdminSecretKey2024";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    
    // Token有效期：24小时
    private static final long EXPIRATION = 86400000;
    
    /**
     * 生成JWT Token
     */
    public String generateToken(String username, String role) {
        return generateToken(username, role, null);
    }

    /**
     * 生成JWT Token（带userId）
     */
    public String generateToken(String username, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        if (userId != null) {
            claims.put("userId", userId);
        }
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY, Jwts.SIG.HS256)
                .compact();
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("Token已过期: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.err.println("不支持的Token: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("Token格式错误: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            System.err.println("Token安全错误: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Token为空或非法: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }
    
    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }
    
    /**
     * 检查Token是否即将过期（1小时内）
     */
    public boolean isTokenExpiringSoon(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        Date expiration = claims.getExpiration();
        long now = System.currentTimeMillis();
        long oneHour = 3600000;
        
        return (expiration.getTime() - now) < oneHour;
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }
}
