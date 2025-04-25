package com.quizplatform.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * WebFlux 기반 서비스용 공통 보안 설정 클래스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public abstract class BaseWebFluxSecurityConfig {

    /**
     * 공통 보안 필터 체인 설정
     * @param http ServerHttpSecurity 객체
     * @return 구성된 ServerHttpSecurity
     */
    protected ServerHttpSecurity configureCommonSecurity(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeExchange(exchanges -> {
                    // 공통으로 허용하는 경로 설정
                    exchanges
                            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                            // Swagger/OpenAPI 관련 경로 허용
                            .pathMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                            .pathMatchers("/v3/api-docs/**", "/swagger-resources/**", "/api-docs/**", "/webjars/**").permitAll()
                            .pathMatchers("/api/*/api-docs/**", "/api/*/swagger-ui/**").permitAll()

                            // Health 체크 및 Actuator 엔드포인트
                            .pathMatchers("/actuator/**").permitAll();

                    // 서비스별 추가 권한 설정 위임
                    configureAdditionalAuthorization(exchanges);

                    // 그 외 모든 API 요청은 인증 필요
                    exchanges.anyExchange().authenticated();
                });
    }

    @Bean
    public abstract SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http);

    /**
     * 각 서비스별 추가 권한 설정을 위한 추상 메소드
     * @param exchanges 권한 교환 설정자
     */
    protected abstract void configureAdditionalAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges);

    /**
     * 공통 CORS 설정
     */
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}