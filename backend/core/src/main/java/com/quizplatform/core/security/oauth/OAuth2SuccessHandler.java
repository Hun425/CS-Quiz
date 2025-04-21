package com.quizplatform.core.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * OAuth2 인증 성공 시 후처리를 담당하는 핸들러 인터페이스
 * <p>
 * 인증된 사용자 정보를 기반으로 JWT 토큰을 생성하고,
 * 클라이언트로 적절히 전달하는 역할을 합니다.
 * 실제 구현체는 user 모듈에서 제공됩니다.
 * </p>
 */
public interface OAuth2SuccessHandler extends AuthenticationSuccessHandler {

    /**
     * OAuth2 인증이 성공적으로 완료되었을 때 호출되는 메서드
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 성공한 인증 정보 (사용자 정보 포함)
     * @throws IOException 리다이렉트 중 I/O 오류 발생 시
     */
    @Override
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws IOException;
}