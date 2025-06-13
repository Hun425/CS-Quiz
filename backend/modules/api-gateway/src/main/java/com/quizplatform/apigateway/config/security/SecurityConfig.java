package com.quizplatform.apigateway.config.security;

import com.quizplatform.apigateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        // Swagger UI 관련 모든 경로 허용
                        .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        // API 문서 접근 허용
                        .pathMatchers("/api-docs/**", "/swagger-config", "/swagger-resources/**").permitAll()
                        // OAuth2 콜백 및 토큰 갱신만 허용
                        .pathMatchers("/api/auth/oauth2/callback", "/api/auth/refresh", "/api/auth/health").permitAll()
                        // 서비스 목록 조회 허용 (개발 환경)
                        .pathMatchers("/api/services").permitAll()
                        // 루트 경로 리디렉션 허용
                        .pathMatchers("/").permitAll()
                        // 나머지는 인증 필요
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
}
