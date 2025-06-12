package com.quizplatform.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    private final JwtTokenProvider tokenProvider;
    private static final List<String> WHITE_LIST = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",
            "/actuator",
            "/api/auth"  // 인증 API는 화이트리스트에 추가
    );

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token != null && tokenProvider.validateToken(token)) {
            // 액세스 토큰인지 확인
            if (!tokenProvider.isAccessToken(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            Claims claims = tokenProvider.getClaims(token);
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.set("X-User-Id", claims.getSubject());
                        headers.set("X-User-Email", claims.get("email", String.class));
                        Object roles = claims.get("roles");
                        if (roles != null) {
                            headers.set("X-User-Roles", roles.toString());
                        }
                    })).build();
            return chain.filter(mutated);
        }
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
