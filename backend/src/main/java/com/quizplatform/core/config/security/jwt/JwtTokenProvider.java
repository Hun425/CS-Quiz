package com.quizplatform.core.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMilliseconds;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityInMilliseconds;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }



    /**
     * 액세스 토큰의 만료 시간을 밀리초 단위로 반환합니다.
     * 이 값은 application.yml에서 설정된 jwt.access-token-validity 값을 사용합니다.
     *
     * @return 액세스 토큰의 만료 시간 (밀리초)
     */
    public long getAccessTokenExpirationMs() {
        return accessTokenValidityInMilliseconds;
    }

    public Claims getClaimsFromToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                throw new InvalidTokenException("Token is empty or null");
            }

            // Bearer 접두사 처리
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token is expired: {}", e.getMessage());
            throw new TokenExpiredException("Token has expired");
        } catch (JwtException e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token");
        }
    }




    /**
     * 액세스 토큰이 곧 만료되는지 확인합니다.
     * 기본적으로 만료 시간의 20% 미만이 남았을 때 토큰 갱신이 필요하다고 판단합니다.
     *
     * @param token JWT 토큰
     * @return 토큰 갱신 필요 여부
     */
    public boolean shouldRefreshToken(String token) {
        long remainingTime = getRemainingExpirationMs(token);
        long refreshThreshold = accessTokenValidityInMilliseconds / 5; // 20% threshold
        return remainingTime > 0 && remainingTime < refreshThreshold;
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
