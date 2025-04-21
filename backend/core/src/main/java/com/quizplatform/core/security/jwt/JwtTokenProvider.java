package com.quizplatform.core.security.jwt;

import org.springframework.security.core.Authentication;

/**
 * JWT 토큰 생성 및 관리를 위한 인터페이스
 * <p>
 * 이 인터페이스는 JWT 토큰 생성, 검증, 파싱 등의 기능을 정의합니다.
 * 구현체는 user 모듈에서 제공됩니다.
 * </p>
 */
public interface JwtTokenProvider {

    /**
     * 주어진 Authentication 객체를 기반으로 Access Token을 생성합니다.
     *
     * @param authentication Spring Security Authentication 객체
     * @return 생성된 Access Token
     */
    String generateAccessToken(Authentication authentication);

    /**
     * 주어진 Authentication 객체를 기반으로 Refresh Token을 생성합니다.
     *
     * @param authentication Spring Security Authentication 객체
     * @return 생성된 Refresh Token
     */
    String generateRefreshToken(Authentication authentication);

    /**
     * 주어진 JWT 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    String getUserIdFromToken(String token);

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    boolean validateToken(String token);

    /**
     * Access Token이 곧 만료되어 갱신이 필요한지 여부를 확인합니다.
     *
     * @param token 확인할 Access Token
     * @return 토큰 갱신이 필요하면 true, 아니면 false
     */
    boolean shouldRefreshToken(String token);

    /**
     * 특정 사용자의 토큰을 무효화합니다.
     *
     * @param userId 무효화할 토큰의 대상 사용자 ID
     */
    void invalidateToken(Long userId);
    
    /**
     * Refresh Token의 만료 시간(밀리초)을 반환합니다.
     *
     * @return Refresh Token 만료 시간 (밀리초)
     */
    long getRefreshTokenExpirationMs();
    
    /**
     * Access Token의 만료 시간(밀리초)을 반환합니다.
     *
     * @return Access Token 만료 시간 (밀리초)
     */
    long getAccessTokenExpirationMs();
}