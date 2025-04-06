package com.quizplatform.core.config.security.jwt;

import com.quizplatform.core.exception.InvalidTokenException;
import com.quizplatform.core.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final RedisTemplate<String, String> redisTemplate;


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

    // 리프레시 토큰 만료 시간을 반환하는 메소드
    public long getRefreshTokenExpirationMs() {
        return refreshTokenValidityInMilliseconds;
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

    /**
     * 주어진 토큰의 남은 유효 시간을 밀리초 단위로 계산합니다.
     * 토큰이 이미 만료된 경우 0을 반환합니다.
     *
     * @param token JWT 토큰
     * @return 남은 유효 시간 (밀리초)
     */
    public long getRemainingExpirationMs(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            return Math.max(0, expiration.getTime() - now.getTime());
        } catch (ExpiredJwtException e) {
            return 0;
        }
    }



    public boolean validateToken(String token) {
        try {
            log.debug("Validating token: {}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : null);
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token is valid");
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }



    /**
     * 사용자의 토큰을 무효화합니다.
     * 이 메소드는 로그아웃 시 호출되며, 해당 사용자의 모든 토큰을 블랙리스트에 추가합니다.
     *
     * @param userId 무효화할 토큰의 사용자 ID
     */
    public void invalidateToken(Long userId) {
        try {
            // 블랙리스트 키 생성 (예: "blacklist:userId")
            String blacklistKey = "blacklist:" + userId.toString();

            // 현재 시간을 저장 (토큰이 무효화된 시점)
            String blacklistValue = String.valueOf(Instant.now().getEpochSecond());

            // Redis에 저장하고 만료 시간 설정
            // 만료 시간은 리프레시 토큰의 남은 유효 기간과 동일하게 설정
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    blacklistValue,
                    Duration.ofMillis(getRefreshTokenExpirationMs())
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to invalidate token", e);
        }
    }
}
