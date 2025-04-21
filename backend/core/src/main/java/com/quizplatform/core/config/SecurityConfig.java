package com.quizplatform.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 기본 보안 설정 클래스
 * <p>
 * 코어 모듈에서는 기본적인 보안 설정만 제공합니다.
 * 자세한 보안 설정은 각 모듈과 API 게이트웨이에서 제공합니다.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 인증이 필요 없는 경로(화이트 리스트) 목록
     */
    public static final String[] WHITE_LIST = {
            "/",
            "/api/v1/auth/**",
            "/api/test-auth/**",
            "/swagger-ui/**",
            "/api/swagger-ui/**",
            "/api/swagger-ui.html",
            "/swagger-resources/**",
            "/api/swagger-resources/**",
            "/v3/api-docs/**",
            "/api/v3/api-docs/**",
            "/api-docs/**",
            "/api/api-docs/**",
            "/api/v1/**",
            "/api/oauth2/**",
            "/h2-console/**",
            "/webjars/**",
            "/api/webjars/**",
            "/api/quizzes/**",
            "/oauth2/**",
            "/ws-battle/**",  // WebSocket 엔드포인트
            "/topic/**",      // STOMP 구독 경로
            "/app/**"         // STOMP 메시지 발행 경로
    };

    /**
     * 비밀번호 인코더 빈 등록
     * 
     * @return BCrypt 알고리즘을 사용하는 PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}