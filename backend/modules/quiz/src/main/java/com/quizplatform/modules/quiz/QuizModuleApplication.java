package com.quizplatform.modules.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 퀴즈 관리 모듈의 진입점 클래스
 * <p>
 * 이 모듈은 퀴즈 생성, 수정, 삭제, 조회 및 퀴즈 풀이 관련 기능을 제공합니다.
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.quizplatform.core", 
    "com.quizplatform.modules.quiz",
    "com.quizplatform.modules.user",
    "com.quizplatform.modules.tag"
})
@EntityScan(basePackages = {
    "com.quizplatform.core",
    "com.quizplatform.modules.quiz.domain",
    "com.quizplatform.modules.user.domain",
    "com.quizplatform.modules.tag.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.quizplatform.modules.quiz.repository"
})
public class QuizModuleApplication {

    /**
     * 퀴즈 모듈 애플리케이션 실행 메서드
     * 
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(QuizModuleApplication.class, args);
    }
}