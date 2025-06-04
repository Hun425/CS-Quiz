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
            "/actuator"
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
            Claims claims = tokenProvider.getClaims(token);
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.set("X-User-Id", claims.getSubject());
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
