package com.quizplatform.apigateway.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * OAuth2 인증 성공 후 JWT 토큰을 발급하고 응답에 추가하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private static final String REDIRECT_URI = "/"; // 프론트엔드 메인 페이지로 리다이렉트

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return Mono.error(new IllegalStateException("예상치 못한 인증 유형입니다: " + authentication.getClass().getName()));
        }
        
        try {
            // JWT 토큰 생성
            String jwtToken = tokenProvider.generateToken(authentication);
            String userId = tokenProvider.getUserIdFromToken(jwtToken);
            String refreshToken = tokenProvider.generateRefreshToken(userId);
            
            // 쿠키에 토큰 저장 (기본 보안을 위해 httpOnly 사용)
            ResponseCookie jwtCookie = ResponseCookie.from("access_token", jwtToken)
                    .httpOnly(true)
                    .secure(exchange.getRequest().getSslInfo() != null) // HTTPS인 경우만 secure 설정
                    .path("/")
                    .maxAge(3600) // 1시간
                    .build();
            
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(exchange.getRequest().getSslInfo() != null)
                    .path("/")
                    .maxAge(604800) // 7일
                    .build();
            
            // 응답 헤더에 쿠키 추가
            exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
            exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            
            // 리다이렉트 (인증 성공 후 프론트엔드로 이동)
            exchange.getResponse().getHeaders().add(HttpHeaders.LOCATION, REDIRECT_URI);
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
            
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            log.error("인증 성공 처리 중 오류 발생: ", e);
            return Mono.error(e);
        }
    }
}
