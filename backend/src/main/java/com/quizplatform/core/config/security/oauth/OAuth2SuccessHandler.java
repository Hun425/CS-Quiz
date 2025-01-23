package com.quizplatform.core.config.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate; // Redis 추가
    private final ObjectMapper objectMapper; // JSON 처리를 위해 추가

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 이미 응답이 커밋되었는지 확인
        if (response.isCommitted()) {
            logger.debug("Response has already been committed");
            return;
        }

       UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // JWT 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // Redis에 리프레시 토큰 저장
        saveRefreshToken(userPrincipal.getId(), refreshToken);

        // 토큰 만료 시간 계산
        long expiresIn = tokenProvider.getAccessTokenExpirationMs();

        // 응답 데이터 구성
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .email(userPrincipal.getEmail())
                .username(userPrincipal.getUsername())
                .build();

        // 프론트엔드로 리다이렉트 (인코딩된 토큰 정보와 함께)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token_info", URLEncoder.encode(
                        objectMapper.writeValueAsString(authResponse),
                        StandardCharsets.UTF_8.toString()))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void saveRefreshToken(UUID userId, String refreshToken) {
        String key = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofDays(14) // 리프레시 토큰 14일 유효
        );
    }
}