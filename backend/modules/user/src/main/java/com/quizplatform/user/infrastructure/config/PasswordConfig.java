package com.quizplatform.user.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 설정
 */
@Configuration
public class PasswordConfig {
    
    /**
     * 비밀번호 인코더 빈 등록
     * BCrypt 알고리즘을 사용하여 비밀번호를 안전하게 해시화
     * 
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}