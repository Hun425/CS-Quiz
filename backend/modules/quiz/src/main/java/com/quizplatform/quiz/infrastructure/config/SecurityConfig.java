package com.quizplatform.quiz.infrastructure.config;

import com.quizplatform.common.security.BaseSecurityConfig;
import com.quizplatform.common.security.JwtTokenUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Quiz 서비스의 Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    public SecurityConfig(JwtTokenUtil jwtTokenUtil) {
        super(jwtTokenUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 공통 보안 설정 적용 후 빌드
        return configureCommonSecurity(http)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configureAuthorization(Object authorize) {
        // Quiz 서비스에 특화된 추가 보안 설정
        // 런타임에는 올바른 타입으로 캐스팅되어 작동함
        try {
            var registry = authorize.getClass().getMethod("anyRequest").invoke(authorize);
            registry.getClass().getMethod("authenticated").invoke(registry);
        } catch (Exception e) {
            throw new RuntimeException("Security configuration error", e);
        }
    }
} 