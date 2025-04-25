package com.quizplatform.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 모든 마이크로서비스에서 공통으로 사용하는 기본 보안 설정 클래스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@RequiredArgsConstructor
public abstract class BaseSecurityConfig {

    protected final JwtTokenUtil jwtTokenUtil;

    /**
     * 공통 보안 필터 체인 설정
     * @param http HttpSecurity 객체
     * @return 구성된 HttpSecurity
     */
    protected HttpSecurity configureCommonSecurity(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> {
                // 공통으로 허용하는 경로 설정
                authorize
                        // Actuator 엔드포인트 허용
                        .requestMatchers("/actuator/**").permitAll()
                        // Swagger/OpenAPI 관련 경로 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/webjars/**").permitAll()
                        // Public 리소스 접근 허용
                        .requestMatchers("/public/**").permitAll();

                
                // // 서비스별 추가 권한 설정 위임 (람다식으로 변경)
                // configureAuthorization(authorize);

                // 모든 요청 허용 (테스트용) - 각 서비스의 configureAuthorization 메소드를 호출한 후에 추가
                authorize.anyRequest().permitAll();
            });
            
        // 필터 및 추가 설정을 위한 훅 메소드 호출
        configureAdditionalSecurity(http);
            
        return http;
    }

    /**
     * 추가 보안 설정을 위한 훅 메소드 (기본 구현은 아무것도 하지 않음)
     * @param http HttpSecurity 객체
     */
    protected void configureAdditionalSecurity(HttpSecurity http) throws Exception {
        // 기본 구현은 아무것도 하지 않음
    }

    /**
     * 각 서비스별 추가 권한 설정을 위한 추상 메소드
     * @param authorize 권한 설정자
     */
    protected abstract void configureAuthorization(Object authorize);

    /**
     * 공통 CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
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