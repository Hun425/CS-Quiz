package com.quizplatform.modules.battle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 배틀 관리 모듈의 진입점 클래스
 * <p>
 * 이 모듈은 실시간 퀴즈 대결, 배틀 방 생성, 매칭, 결과 집계 등의 기능을 제공합니다.
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.quizplatform.core",
    "com.quizplatform.modules.battle",
    "com.quizplatform.modules.user",
    "com.quizplatform.modules.quiz"
})
@EntityScan(basePackages = {
    "com.quizplatform.core",
    "com.quizplatform.modules.battle.domain",
    "com.quizplatform.modules.user.domain",
    "com.quizplatform.modules.quiz.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.quizplatform.modules.battle.repository"
})
public class BattleModuleApplication {

    /**
     * 배틀 모듈 애플리케이션 실행 메서드
     * 
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(BattleModuleApplication.class, args);
    }
}