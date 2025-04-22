package com.quizplatform.common.adapter.config.security.jwt;

// TODO: Exception 클래스들의 최종 위치 확인 후 import 경로 재검토 필요
import com.quizplatform.common.exception.InvalidTokenException;
import com.quizplatform.common.exception.TokenExpiredException;
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
            throw new TokenExpiredException("토큰이 만료되었습니다.");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
            throw new InvalidTokenException("잘못된 형식의 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new InvalidTokenException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty or null: {}", e.getMessage());
            throw new InvalidTokenException("JWT 토큰이 비어있거나 잘못되었습니다.");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw new InvalidTokenException("JWT 서명 검증에 실패했습니다.");
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("Unexpected error parsing JWT token: {}", e.getMessage(), e);
            throw new InvalidTokenException("토큰 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 토큰이 리프레시 되어야 하는지 여부를 판단합니다.
     * 현재는 만료 30분 전일 경우 리프레시 대상으로 판단합니다.
     *
     * @param token 검사할 JWT 토큰
     * @return 리프레시 필요 여부 (true: 필요함, false: 필요 없음)
     */
    public boolean shouldRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long remainingMillis = expiration.getTime() - System.currentTimeMillis();

            // 만료 30분 전 확인 (30 * 60 * 1000 밀리초)
            boolean shouldRefresh = remainingMillis <= (30 * 60 * 1000);
            if (shouldRefresh) {
                log.debug("Token is due for refresh. Remaining time: {} ms", remainingMillis);
            } else {
                log.trace("Token refresh not needed yet. Remaining time: {} ms", remainingMillis);
            }
            return shouldRefresh;
        } catch (TokenExpiredException e) {
            log.debug("Token already expired, refresh needed.");
            // 이미 만료된 토큰도 리프레시 대상
            return true;
        } catch (InvalidTokenException e) {
            log.warn("Invalid token encountered while checking refresh status: {}", e.getMessage());
            // 유효하지 않은 토큰은 리프레시 대상 아님
            return false;
        }
    }

    /**
     * 토큰의 남은 만료 시간을 밀리초 단위로 계산합니다.
     * 토큰이 유효하지 않거나 이미 만료된 경우 0 또는 음수 값을 반환할 수 있습니다.
     *
     * @param token 검사할 JWT 토큰
     * @return 남은 만료 시간 (밀리초), 유효하지 않으면 0 또는 음수
     */
    public long getRemainingExpirationMs(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            log.trace("Remaining expiration time for token: {} ms", remaining);
            return remaining > 0 ? remaining : 0;
        } catch (TokenExpiredException e) {
            log.debug("Token is already expired, remaining time is 0.");
            return 0;
        } catch (InvalidTokenException e) {
            log.warn("Invalid token encountered while calculating remaining expiration: {}", e.getMessage());
            return 0; // 유효하지 않은 토큰은 남은 시간 0으로 처리
        }
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     * 서명 검증, 만료 여부, Redis 블랙리스트 포함 여부를 확인합니다.
     *
     * @param token 검증할 JWT 토큰 문자열 ("Bearer " 접두사 포함 가능)
     * @return 토큰 유효성 여부 (true: 유효함, false: 유효하지 않음)
     */
    public boolean validateToken(String token) {
        try {
            // Bearer 접두사 제거
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 토큰이 비어있는지 확인
            if (!StringUtils.hasText(token)) {
                log.warn("Validation failed: Token is empty or null.");
                return false;
            }

            // Redis 블랙리스트 확인
            String isBlacklisted = redisTemplate.opsForValue().get("blacklist:" + token);
            if (isBlacklisted != null) {
                log.warn("Validation failed: Token is blacklisted.");
                return false;
            }

            // Jwts 라이브러리를 이용한 파싱 및 검증 (서명, 만료 등)
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.debug("Token validation successful.");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Validation failed: Token expired - {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Validation failed: Invalid JWT token format - {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Validation failed: Unsupported JWT token - {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Validation failed: JWT claims string is empty or invalid - {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("Validation failed: JWT signature validation failed - {}", e.getMessage());
        } catch (Exception e) {
            // 예상치 못한 예외 처리
            log.error("Validation failed: Unexpected error - {}", e.getMessage(), e);
        }
        return false; // 유효하지 않은 경우
    }

    /**
     * 사용자 로그아웃 시 Access Token과 Refresh Token을 Redis 블랙리스트에 추가합니다.
     * 토큰의 남은 유효 기간 동안 Redis에 저장하여 재사용을 방지합니다.
     *
     * @param userId 로그아웃하는 사용자 ID (추후 다른 용도로 활용 가능성 있음, 현재는 미사용)
     * @param accessToken 무효화할 Access Token
     * @param refreshToken 무효화할 Refresh Token (null 가능)
     */
    public void invalidateToken(Long userId, String accessToken, String refreshToken) {
        // Access Token 블랙리스트 추가
        if (StringUtils.hasText(accessToken)) {
            String cleanAccessToken = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
            long remainingExpirationMs = getRemainingExpirationMs(cleanAccessToken);
            if (remainingExpirationMs > 0) {
                redisTemplate.opsForValue().set("blacklist:" + cleanAccessToken, "logout", Duration.ofMillis(remainingExpirationMs));
                log.info("Access Token for user {} blacklisted for {} ms", userId, remainingExpirationMs);
            } else {
                log.debug("Access Token for user {} already expired or invalid, not blacklisting.", userId);
            }
        }

        // Refresh Token 블랙리스트 추가 (존재할 경우)
        if (StringUtils.hasText(refreshToken)) {
            String cleanRefreshToken = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
            long remainingRefreshExpirationMs = getRemainingExpirationMs(cleanRefreshToken);
            if (remainingRefreshExpirationMs > 0) {
                redisTemplate.opsForValue().set("blacklist:" + cleanRefreshToken, "logout", Duration.ofMillis(remainingRefreshExpirationMs));
                log.info("Refresh Token for user {} blacklisted for {} ms", userId, remainingRefreshExpirationMs);
            } else {
                log.debug("Refresh Token for user {} already expired or invalid, not blacklisting.", userId);
            }
        }
    }
} 