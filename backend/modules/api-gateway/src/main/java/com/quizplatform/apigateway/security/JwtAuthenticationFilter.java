package com.quizplatform.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT 토큰을 검증하고 헤더에 사용자 정보를 추가하는 전역 필터
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtTokenProvider tokenProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Swagger UI 및 관련 리소스는 JWT 인증 필터 건너뛰기
        if (path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs")) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange.getRequest());

        try {
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                Claims claims = tokenProvider.getClaims(token);
                
                // 추출한 사용자 정보로 요청 헤더를 추가
                ServerHttpRequest request = addAuthHeaders(exchange.getRequest(), claims);
                
                // 변경된 요청 헤더로 교체
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(request)
                        .build();
                
                return chain.filter(mutatedExchange);
            }
        } catch (Exception e) {
            log.warn("JWT 필터 처리 중 오류: {}", e.getMessage());
        }
        
        // 유효한 토큰이 없어도 요청은 계속 진행 (인증 필요 경로는 SecurityConfig에서 처리)
        return chain.filter(exchange);
    }
    
    /**
     * 요청에서 JWT 토큰 추출
     */
    private String resolveToken(ServerHttpRequest request) {
        // 1. Authorization 헤더에서 먼저 확인
        String bearerToken = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        
        // 2. 쿠키에서 확인
        List<HttpCookie> cookies = request.getCookies().get("access_token");
        if (cookies != null && !cookies.isEmpty()) {
            return cookies.get(0).getValue();
        }
        
        return null;
    }
    
    /**
     * 요청 헤더에 사용자 인증 정보 추가
     */
    private ServerHttpRequest addAuthHeaders(ServerHttpRequest request, Claims claims) {
        // 기존 인증 헤더 유지 (토큰을 그대로 전달)
        if (!request.getHeaders().containsKey(AUTHORIZATION_HEADER) && 
            request.getCookies().containsKey("access_token")) {
            
            List<HttpCookie> cookies = request.getCookies().get("access_token");
            if (!cookies.isEmpty()) {
                String token = cookies.get(0).getValue();
                
                return request.mutate()
                        .header(AUTHORIZATION_HEADER, TOKEN_PREFIX + token)
                        .build();
            }
        }
        
        // 기존 방식도 함께 유지 (하위 호환성)
        return request.mutate()
                .header("X-Auth-User-Id", claims.getSubject())
                .header("X-Auth-User-Name", claims.get("name", String.class))
                .header("X-Auth-Provider", claims.get("provider", String.class))
                .header("X-Auth-Roles", String.valueOf(claims.get("roles")))
                .build();
    }
}
