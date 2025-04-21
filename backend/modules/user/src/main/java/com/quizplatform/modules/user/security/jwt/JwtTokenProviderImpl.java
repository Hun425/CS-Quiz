package com.quizplatform.modules.user.security.jwt;

import com.quizplatform.core.exception.InvalidTokenException;
import com.quizplatform.core.exception.TokenExpiredException;
import com.quizplatform.core.security.jwt.JwtTokenProvider;
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

/**
 * JWT 토큰 생성 및 관리를 위한 구현체
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProviderImpl implements JwtTokenProvider {

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
        log.info("JWT signing key initialized.");
    }

    @Override
    public long getRefreshTokenExpirationMs() {
        return refreshTokenValidityInMilliseconds;
    }

    @Override
    public String generateAccessToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();

        log.debug("Generated Access Token for user: {}", authentication.getName());
        return accessToken;
    }

    @Override
    public String generateRefreshToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();

        log.debug("Generated Refresh Token for user: {}", authentication.getName());
        return refreshToken;
    }

    @Override
    public String getUserIdFromToken(String token) {
        try {
            log.trace("Attempting to extract user ID from token...");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            log.debug("Successfully extracted user ID: {}", userId);
            return userId;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired while extracting user ID: {}", e.getMessage());
            throw new TokenExpiredException("토큰이 만료되었습니다.");
        } catch (JwtException e) {
            log.error("JWT parsing error while extracting user ID: {}", e.getMessage(), e);
            throw new InvalidTokenException("유효하지 않은 JWT 토큰입니다.");
        } catch (Exception e) {
            log.error("Unexpected error extracting user ID from token: {}", e.getMessage(), e);
            throw new InvalidTokenException("토큰 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public long getAccessTokenExpirationMs() {
        return accessTokenValidityInMilliseconds;
    }

    public Claims getClaimsFromToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                log.warn("Attempted to parse claims from an empty or null token.");
                throw new InvalidTokenException("Token is empty or null");
            }

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
            log.error("Error parsing JWT token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Invalid JWT token: " + e.getMessage());
        }
    }

    @Override
    public boolean shouldRefreshToken(String token) {
        try {
            long remainingTime = getRemainingExpirationMs(token);
            long refreshThreshold = accessTokenValidityInMilliseconds / 5;
            boolean shouldRefresh = remainingTime > 0 && remainingTime < refreshThreshold;
            log.trace("Token remaining time: {}ms, Threshold: {}ms, Should refresh: {}", remainingTime, refreshThreshold, shouldRefresh);
            return shouldRefresh;
        } catch (Exception e) {
            log.warn("Could not determine if token needs refresh due to error: {}", e.getMessage());
            return false;
        }
    }

    public long getRemainingExpirationMs(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            return Math.max(0, expiration.getTime() - now.getTime());
        } catch (TokenExpiredException e) {
            return 0;
        } catch (Exception e) {
            log.error("Error calculating remaining expiration time: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                log.warn("Validation failed: Token is empty or null.");
                return false;
            }

            log.trace("Validating token: {}...", token.substring(0, Math.min(20, token.length())));

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            log.debug("Token validation successful.");
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature or structure: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation failed due to unexpected error: {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void invalidateToken(Long userId) {
        if (userId == null) {
            log.error("Cannot invalidate token for null userId.");
            return;
        }
        try {
            String blacklistKey = "blacklist:" + userId.toString();
            String blacklistValue = String.valueOf(Instant.now().getEpochSecond());

            redisTemplate.opsForValue().set(
                    blacklistKey,
                    blacklistValue,
                    Duration.ofMillis(getRefreshTokenExpirationMs())
            );
            log.info("Token invalidated for user ID {}. Blacklist entry set with expiration.", userId);

        } catch (Exception e) {
            log.error("Failed to invalidate token for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to invalidate token due to Redis error", e);
        }
    }
}