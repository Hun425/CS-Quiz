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

/**
 * JWT(Json Web Token) 생성, 검증, 파싱 및 관리 기능을 제공하는 컴포넌트입니다.
 * Access Token과 Refresh Token을 생성하고, 토큰의 유효성을 검사하며,
 * 토큰에서 사용자 정보를 추출하는 역할을 담당합니다.
 * 또한 Redis를 이용한 토큰 무효화(블랙리스트) 기능을 포함합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    /** Redis 연동을 위한 Template 객체 (토큰 블랙리스트 등에 활용) */
    private final RedisTemplate<String, String> redisTemplate;

    /** JWT 서명 및 검증에 사용될 비밀키 (application.yml 등에서 주입) */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** Access Token의 유효 기간 (밀리초 단위, application.yml 등에서 주입) */
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMilliseconds;

    /** Refresh Token의 유효 기간 (밀리초 단위, application.yml 등에서 주입) */
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityInMilliseconds;

    /** JWT 서명 및 검증에 사용될 Key 객체 */
    private Key key;

    /**
     * 빈 초기화 시 jwtSecret 값을 사용하여 HMAC-SHA 키를 생성합니다.
     * 이 키는 이후 토큰 생성 및 검증에 사용됩니다.
     */
    @PostConstruct
    public void init() {
        // jwtSecret 바이트 배열로부터 안전한 HMAC-SHA 키 생성
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        log.info("JWT signing key initialized.");
    }

    /**
     * 설정된 Refresh Token의 만료 시간(유효 기간)을 밀리초 단위로 반환합니다.
     *
     * @return Refresh Token 만료 시간 (밀리초)
     */
    public long getRefreshTokenExpirationMs() {
        return refreshTokenValidityInMilliseconds;
    }

    /**
     * 주어진 Authentication 객체를 기반으로 Access Token을 생성합니다.
     * 토큰의 Subject에는 사용자 식별자(authentication.getName())가 포함됩니다.
     *
     * @param authentication Spring Security Authentication 객체 (사용자 정보 포함)
     * @return 생성된 Access Token 문자열
     */
    public String generateAccessToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName()) // 토큰 주체 (사용자 식별자)
                .setIssuedAt(now)                     // 발급 시각
                .setExpiration(expiryDate)            // 만료 시각
                .signWith(key)                        // 서명 키 지정
                .compact();                           // 토큰 생성

        log.debug("Generated Access Token for user: {}", authentication.getName());
        return accessToken;
    }

    /**
     * 주어진 Authentication 객체를 기반으로 Refresh Token을 생성합니다.
     * 토큰의 Subject에는 사용자 식별자(authentication.getName())가 포함됩니다.
     * Access Token보다 긴 유효 기간을 가집니다.
     *
     * @param authentication Spring Security Authentication 객체 (사용자 정보 포함)
     * @return 생성된 Refresh Token 문자열
     */
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

    /**
     * 주어진 JWT 토큰에서 사용자 ID (Subject 클레임)를 추출합니다.
     * 토큰 문자열 앞의 "Bearer " 접두사를 자동으로 제거합니다.
     * 토큰이 만료되었거나 유효하지 않은 경우 적절한 예외를 발생시킵니다.
     *
     * @param token JWT 토큰 문자열 (선택적으로 "Bearer " 접두사 포함 가능)
     * @return 추출된 사용자 ID 문자열
     * @throws TokenExpiredException 토큰이 만료된 경우
     * @throws InvalidTokenException 토큰이 유효하지 않거나 파싱 오류 발생 시
     */
    public String getUserIdFromToken(String token) {
        try {
            log.trace("Attempting to extract user ID from token...");

            // Bearer 접두사 제거
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 토큰 파싱 및 클레임 추출
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key) // 검증 키 설정
                    .build()
                    .parseClaimsJws(token) // 토큰 파싱 및 서명 검증
                    .getBody(); // 클레임(페이로드) 부분 가져오기

            String userId = claims.getSubject(); // Subject 클레임 (사용자 ID) 추출
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

    /**
     * 설정된 Access Token의 만료 시간(유효 기간)을 밀리초 단위로 반환합니다.
     * 이 값은 application.yml에서 설정된 jwt.access-token-validity 값을 사용합니다.
     *
     * @return Access Token의 만료 시간 (밀리초)
     */
    public long getAccessTokenExpirationMs() {
        return accessTokenValidityInMilliseconds;
    }

    /**
     * 주어진 JWT 토큰을 파싱하여 클레임(Claims) 객체를 반환합니다.
     * 토큰 문자열 앞의 "Bearer " 접두사를 자동으로 제거합니다.
     * 토큰이 비어있거나, 만료되었거나, 유효하지 않은 경우 예외를 발생시킵니다.
     *
     * @param token JWT 토큰 문자열 (선택적으로 "Bearer " 접두사 포함 가능)
     * @return 토큰의 클레임 정보를 담은 Claims 객체
     * @throws InvalidTokenException 토큰이 비어있거나 null, 또는 유효하지 않은 경우
     * @throws TokenExpiredException 토큰이 만료된 경우
     */
    public Claims getClaimsFromToken(String token) {
        try {
            // 토큰이 비어있는지 확인
            if (!StringUtils.hasText(token)) {
                log.warn("Attempted to parse claims from an empty or null token.");
                throw new InvalidTokenException("Token is empty or null");
            }

            // Bearer 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 토큰 파싱 및 클레임 반환
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰 예외 처리
            log.warn("Token is expired: {}", e.getMessage());
            throw new TokenExpiredException("Token has expired");
        } catch (JwtException e) {
            // 그 외 JWT 관련 예외 처리 (파싱, 서명 등)
            log.error("Error parsing JWT token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Invalid JWT token: " + e.getMessage());
        }
    }

    /**
     * Access Token이 곧 만료되어 갱신이 필요한지 여부를 확인합니다.
     * 기본적으로 설정된 Access Token 유효 기간의 20% 미만이 남았을 때 true를 반환합니다.
     *
     * @param token 확인할 Access Token 문자열
     * @return 토큰 갱신이 필요하면 true, 아니면 false
     */
    public boolean shouldRefreshToken(String token) {
        try {
            // 토큰의 남은 유효 시간 계산
            long remainingTime = getRemainingExpirationMs(token);
            // 갱신 임계값 계산 (유효 기간의 20%)
            long refreshThreshold = accessTokenValidityInMilliseconds / 5;
            // 남은 시간이 0보다 크고 임계값보다 작으면 갱신 필요
            boolean shouldRefresh = remainingTime > 0 && remainingTime < refreshThreshold;
            log.trace("Token remaining time: {}ms, Threshold: {}ms, Should refresh: {}", remainingTime, refreshThreshold, shouldRefresh);
            return shouldRefresh;
        } catch (Exception e) {
            // 토큰 파싱 중 에러 발생 시 (만료 포함) 갱신 불필요
            log.warn("Could not determine if token needs refresh due to error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 주어진 토큰의 남은 유효 시간을 밀리초 단위로 계산합니다.
     * 토큰이 이미 만료된 경우 0을 반환합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 남은 유효 시간 (밀리초), 만료 시 0
     */
    public long getRemainingExpirationMs(String token) {
        try {
            // 토큰에서 클레임 정보 추출
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration(); // 만료 시각
            Date now = new Date(); // 현재 시각

            // 남은 시간 계산 (음수일 경우 0 반환)
            return Math.max(0, expiration.getTime() - now.getTime());
        } catch (TokenExpiredException e) {
            // 명시적으로 만료된 경우 0 반환
            return 0;
        } catch (Exception e) {
            // 그 외 파싱 오류 등 발생 시 유효하지 않은 토큰으로 간주하고 0 반환
            log.error("Error calculating remaining expiration time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     * 서명, 만료 시간, 형식 등을 확인하고, 유효하지 않은 경우 로그를 기록하고 false를 반환합니다.
     * 토큰 문자열 앞의 "Bearer " 접두사를 자동으로 제거합니다.
     *
     * @param token 검증할 JWT 토큰 문자열 (선택적으로 "Bearer " 접두사 포함 가능)
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            // 토큰 null 또는 빈 문자열 체크
            if (!StringUtils.hasText(token)) {
                log.warn("Validation failed: Token is empty or null.");
                return false;
            }

            log.trace("Validating token: {}...", token.substring(0, Math.min(20, token.length())));

            // Bearer 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 토큰 파싱 및 검증 (서명, 만료 시간 등 확인)
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
            // 예상치 못한 다른 예외 처리
            log.error("JWT validation failed due to unexpected error: {}", e.getMessage(), e);
        }
        // 예외 발생 시 유효하지 않은 토큰으로 간주
        return false;
    }

    /**
     * 특정 사용자의 토큰을 무효화하기 위해 Redis에 블랙리스트 항목을 추가합니다.
     * 이 메소드는 주로 로그아웃 시 호출됩니다.
     * 블랙리스트 키는 "blacklist:{userId}" 형식이며, 값으로는 무효화 시점의 타임스탬프를 저장합니다.
     * 항목의 만료 시간은 해당 사용자의 Refresh Token 유효 기간과 동일하게 설정하여,
     * Refresh Token이 만료되면 블랙리스트 항목도 자동으로 삭제되도록 합니다.
     *
     * @param userId 무효화할 토큰의 대상 사용자 ID
     * @throws RuntimeException Redis 작업 중 오류 발생 시
     */
    public void invalidateToken(Long userId) {
        if (userId == null) {
            log.error("Cannot invalidate token for null userId.");
            return;
        }
        try {
            // 블랙리스트 키 생성 (예: "blacklist:123")
            String blacklistKey = "blacklist:" + userId.toString();
            // 값으로는 현재 시각의 타임스탬프(초 단위) 저장
            String blacklistValue = String.valueOf(Instant.now().getEpochSecond());

            // Redis에 키-값 저장 및 만료 시간 설정
            // 만료 시간은 Refresh Token의 유효 기간으로 설정
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    blacklistValue,
                    Duration.ofMillis(getRefreshTokenExpirationMs()) // Refresh Token 만료 시간 적용
            );
            log.info("Token invalidated for user ID {}. Blacklist entry set with expiration.", userId);

        } catch (Exception e) {
            // Redis 작업 실패 시 런타임 예외 발생
            log.error("Failed to invalidate token for user ID {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to invalidate token due to Redis error", e);
        }
    }
}