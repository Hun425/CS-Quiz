package com.quizplatform.battle.infrastructure.config; // 패키지 경로는 실제 프로젝트에 맞게 조정하세요

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (람다 스타일)
                .csrf(AbstractHttpConfigurer::disable) // .csrf(csrf -> csrf.disable()) 와 동일

                // CORS 설정 (람다 스타일)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // .and() 제거

                // 세션 관리: STATELESS 설정 (람다 스타일)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // .and() 제거

                // HTTP 요청 인가 설정 (기존 방식 유지 - 이미 람다 스타일)
                .authorizeHttpRequests(authorize -> authorize
                        // API 엔드포인트 허용
                        .requestMatchers("/api/**").permitAll()
                        // Swagger UI 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Actuator 엔드포인트 허용
                        .requestMatchers("/actuator/**").permitAll()
                        // WebSocket 엔드포인트 허용
                        .requestMatchers("/ws-battle/**").permitAll()
                        .requestMatchers("/topic/**").permitAll()
                        .requestMatchers("/app/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );
        // .and() 와 같은 연결자 없이도 다른 설정(예: .httpBasic(), .formLogin() 등)을 바로 체이닝 가능

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 허용할 Origin 설정 (실제 운영 환경에서는 더 구체적인 Origin 사용 권장)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // 필요한 메서드 추가
        configuration.setAllowedHeaders(Arrays.asList("*")); // 실제 운영 환경에서는 필요한 헤더만 명시 권장
        configuration.setAllowCredentials(true); // Credential 허용 여부

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 이 CORS 설정 적용
        return source;
    }
}