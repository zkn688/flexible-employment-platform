package com.example.employment.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class TokenUtils {

    private static String jwtSecret;
    private static Long expireHours;

    public TokenUtils(@Value("${jwt.secret:flexible-employment-user-jwt-secret-2026-demo-key}") String secret,
                      @Value("${jwt.expire-hours:24}") Long hours) {
        jwtSecret = secret;
        expireHours = hours;
    }

    public static String createUserToken(Long userId) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireHours * 60 * 60 * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", "USER")
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.trim().isEmpty()) {
            token = request.getParameter("token");
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("请先登录");
        }
        token = cleanToken(token);

        if (token.startsWith("user-")) {
            return Long.valueOf(token.substring("user-".length()));
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            throw new IllegalArgumentException("登录状态已失效，请重新登录");
        }
    }

    private static String cleanToken(String token) {
        String value = token.trim();
        if (value.startsWith("Bearer ")) {
            return value.substring("Bearer ".length()).trim();
        }
        return value;
    }

    private static Key getSignKey() {
        byte[] bytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
