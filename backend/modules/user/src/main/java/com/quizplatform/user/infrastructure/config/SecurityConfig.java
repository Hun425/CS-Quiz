package com.quizplatform.user.infrastructure.config;

import com.quizplatform.common.security.BaseSecurityConfig;
import com.quizplatform.common.security.JwtTokenUtil;
import com.quizplatform.user.infrastructure.security.JwtAuthenticationFilter;
import com.quizplatform.user.infrastructure.security.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * User 서비스의 Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtTokenUtil jwtTokenUtil, JwtAuthenticationProvider jwtAuthenticationProvider) {
        super(jwtTokenUtil);
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 공통 보안 설정 적용
        return configureCommonSecurity(http)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configureAuthorization(Object authorize) {
        // User 서비스에 특화된 추가 보안 설정
        try {
            // login과 register 경로 허용
            var requestMatchers = authorize.getClass().getMethod("requestMatchers", String[].class)
                    .invoke(authorize, (Object) new String[]{"/login", "/register"});
            requestMatchers.getClass().getMethod("permitAll").invoke(requestMatchers);
            
            // 나머지 경로는 인증 필요
            var registry = authorize.getClass().getMethod("anyRequest").invoke(authorize);
            registry.getClass().getMethod("authenticated").invoke(registry);
        } catch (Exception e) {
            throw new RuntimeException("Security configuration error", e);
        }
    }

    @Override
    protected void configureAdditionalSecurity(HttpSecurity http) throws Exception {
        // 인증 제공자 및 필터 설정
        http.authenticationProvider(jwtAuthenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }
}
