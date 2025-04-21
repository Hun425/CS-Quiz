package com.quizplatform.modules.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터 인터페이스
 * 
 * <p>요청 헤더에서 JWT 토큰을 추출하고 검증하여, 유효한 토큰인 경우
 * Spring Security의 SecurityContext에 인증 정보를 설정하는 필터입니다.</p>
 */
public abstract class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 모든 요청에 대해 JWT 토큰을 검증하고 인증 처리를 수행하는 메서드
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected abstract void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain) throws ServletException, IOException;
}