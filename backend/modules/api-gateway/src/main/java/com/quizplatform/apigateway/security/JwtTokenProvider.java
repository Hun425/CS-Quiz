package com.quizplatform.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token-validity:3600}")  // 1시간 (기본값)
    private long accessTokenValiditySeconds;
    
    @Value("${jwt.refresh-token-validity:604800}")  // 7일 (기본값)
    private long refreshTokenValiditySeconds;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Long userId, String email, List<String> roles) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusSeconds(accessTokenValiditySeconds);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusSeconds(refreshTokenValiditySeconds);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 토큰 쌍 생성 (Access + Refresh)
     */
    public TokenPair generateTokenPair(Long userId, String email, List<String> roles) {
        String accessToken = generateAccessToken(userId, email, roles);
        String refreshToken = generateRefreshToken(userId);
        
        return new TokenPair(accessToken, refreshToken, accessTokenValiditySeconds);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 Claims 추출
     */
    public Claims getClaims(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
    
    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.valueOf(claims.getSubject());
    }
    
    /**
     * 토큰 타입 확인 (access 또는 refresh)
     */
    public boolean isAccessToken(String token) {
        Claims claims = getClaims(token);
        return "access".equals(claims.get("type"));
    }
    
    public boolean isRefreshToken(String token) {
        Claims claims = getClaims(token);
        return "refresh".equals(claims.get("type"));
    }

    /**
     * 토큰 쌍을 나타내는 record 클래스
     */
    public record TokenPair(
        String accessToken,
        String refreshToken,
        long expiresIn
    ) {}
}
