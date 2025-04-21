package com.quizplatform.modules.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 사용자 관리 모듈의 진입점 클래스
 * <p>
 * 이 모듈은 사용자 인증, 권한 관리, 프로필 관리 등의 기능을 제공합니다.
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.quizplatform.core",
    "com.quizplatform.modules.user"
})
@EntityScan(basePackages = {
    "com.quizplatform.core",
    "com.quizplatform.modules.user.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.quizplatform.modules.user.repository"
})
public class UserModuleApplication {

    /**
     * 사용자 모듈 애플리케이션 실행 메서드
     * 
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(UserModuleApplication.class, args);
    }
}