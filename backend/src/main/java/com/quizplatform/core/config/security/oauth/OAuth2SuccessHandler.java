package com.quizplatform.core.config.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.dto.AuthResponse;
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
 * OAuth2 인증 성공 시 후처리를 담당하는 핸들러입니다.
 * 인증된 사용자 정보를 기반으로 JWT Access Token과 Refresh Token을 생성하고,
 * Refresh Token은 Redis에 저장합니다.
 * 최종적으로 설정된 리다이렉트 URI에 토큰 및 사용자 정보를 쿼리 파라미터로 포함하여 리다이렉트합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /** JWT 토큰 생성 및 관리 컴포넌트 */
    private final JwtTokenProvider tokenProvider;
    /** Redis 접근을 위한 Template (Refresh Token 저장용) */
    private final RedisTemplate<String, String> redisTemplate;
    /** JSON 직렬화/역직렬화를 위한 ObjectMapper (현재 코드에서는 직접 사용되지 않음) */
    private final ObjectMapper objectMapper;

    /** OAuth2 인증 성공 후 리다이렉트될 클라이언트 측 URI (application.yml 등에서 주입) */
    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    /**
     * OAuth2 인증이 성공적으로 완료되었을 때 호출되는 메서드입니다.
     * 1. 인증된 사용자 정보(UserPrincipal)를 가져옵니다.
     * 2. Access Token과 Refresh Token을 생성합니다.
     * 3. Refresh Token을 Redis에 저장합니다.
     * 4. 리다이렉트할 Target URL을 구성합니다. (redirectUri + 쿼리 파라미터)
     * 5. 사용자를 Target URL로 리다이렉트 시킵니다.
     *
     * @param request        HTTP 요청 객체
     * @param response       HTTP 응답 객체
     * @param authentication 성공한 인증 정보 (사용자 정보 포함)
     * @throws IOException 리다이렉트 중 I/O 오류 발생 시
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 응답이 이미 커밋(전송 시작)되었는지 확인 (예: 다른 필터에서 이미 응답을 보낸 경우)
        if (response.isCommitted()) {
            log.debug("Response has already been committed.");
            return;
        }

        // 1. 인증 객체로부터 UserPrincipal 추출
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.debug("OAuth2 authentication successful for user: {}", userPrincipal.getEmail());

        // 2. JWT Access Token 및 Refresh Token 생성
        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        log.debug("Generated tokens for user: {}", userPrincipal.getEmail());


        // 3. Redis에 Refresh Token 저장
        saveRefreshToken(userPrincipal.getId(), refreshToken);

        // 4. Access Token 만료 시간 계산 (응답에 포함시키기 위함)
        long expiresIn = tokenProvider.getAccessTokenExpirationMs();

        // 5. 응답 데이터 구성 (AuthResponse DTO 사용) - 실제 응답 본문 대신 리다이렉트 파라미터로 사용
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .email(userPrincipal.getEmail())
                .username(userPrincipal.getUsername())
                .build();

        // 6. 리다이렉트 Target URL 구성
        // 설정된 redirectUri에 토큰, 사용자 정보 등을 쿼리 파라미터로 추가
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", authResponse.getAccessToken())
                .queryParam("refreshToken", authResponse.getRefreshToken())
                .queryParam("email", authResponse.getEmail())
                .queryParam("username", authResponse.getUsername())
                .queryParam("expiresIn", authResponse.getExpiresIn())
                .build().toUriString();

        log.info("Redirecting user {} to target URL: {}", userPrincipal.getEmail(), targetUrl);

        // 7. 리다이렉트 수행 (SimpleUrlAuthenticationSuccessHandler의 기능 사용)
        // clearAuthenticationAttributes(request); // 이전 인증 속성 정리 (필요 시)
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 사용자의 Refresh Token을 Redis에 저장합니다. (내부 헬퍼 메서드)
     * 키는 "refresh_token:{userId}" 형식을 사용하며,
     * 만료 시간은 14일로 설정됩니다. (토큰 자체의 만료 시간과 별개로 Redis 저장 기간)
     *
     * @param userId       토큰을 저장할 사용자의 ID
     * @param refreshToken 저장할 Refresh Token 문자열
     */
    private void saveRefreshToken(Long userId, String refreshToken) {
        // Redis 키 정의
        String key = "refresh_token:" + userId;
        log.debug("Saving refresh token to Redis with key: {}", key);
        try {
            // Redis에 값 설정 (만료 시간 14일 지정)
            redisTemplate.opsForValue().set(
                    key,
                    refreshToken,
                    Duration.ofDays(14) // Redis 저장 만료 기간 설정
            );
            log.info("Refresh token saved successfully for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to save refresh token to Redis for user ID {}: {}", userId, e.getMessage());
            // 여기서 예외를 다시 던지거나 적절한 에러 처리를 고려할 수 있습니다.
        }
    }
}