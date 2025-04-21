package com.quizplatform.modules.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizplatform.modules.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

/**
 * OAuth2 인증 성공 시 후처리를 담당하는 핸들러 구현체
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandlerImpl extends SimpleUrlAuthenticationSuccessHandler implements OAuth2SuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.debug("Response has already been committed.");
            return;
        }

        // 인증 객체로부터 사용자 정보 추출
        String username = authentication.getName();
        log.debug("OAuth2 authentication successful for user: {}", username);

        // JWT 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        log.debug("Generated tokens for user: {}", username);

        // Redis에 Refresh Token 저장
        // 사용자 ID 추출 (Long 타입으로 변환 가정)
        Long userId = Long.parseLong(username);
        saveRefreshToken(userId, refreshToken);

        // Access Token 만료 시간 계산
        long expiresIn = tokenProvider.getAccessTokenExpirationMs();

        // 응답 데이터 구성
        // AuthResponse 클래스에 의존하지 않도록 맵으로 변경
        String email = username; // 실제 구현에서는 인증 객체에서 이메일 추출
        String displayName = username; // 실제 구현에서는 인증 객체에서 사용자명 추출

        // 리다이렉트 URL 구성
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("email", email)
                .queryParam("username", displayName)
                .queryParam("expiresIn", expiresIn)
                .build().toUriString();

        log.info("Redirecting user {} to target URL", username);

        // 리다이렉트 수행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 사용자의 Refresh Token을 Redis에 저장합니다.
     *
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token
     */
    private void saveRefreshToken(Long userId, String refreshToken) {
        String key = "refresh_token:" + userId;
        log.debug("Saving refresh token to Redis with key: {}", key);
        try {
            redisTemplate.opsForValue().set(
                    key,
                    refreshToken,
                    Duration.ofDays(14)
            );
            log.info("Refresh token saved successfully for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to save refresh token to Redis for user ID {}: {}", userId, e.getMessage());
        }
    }
}