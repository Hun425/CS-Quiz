package com.quizplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 퀴즈 플랫폼 애플리케이션의 메인 클래스
 * 스프링 부트 애플리케이션 진입점으로 작동하며 스케줄링 기능을 활성화합니다.
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@SpringBootApplication
@EnableScheduling
public class QuizPlatformApplication {

    /**
     * 애플리케이션 시작 메서드
     * 
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(QuizPlatformApplication.class, args);
    }
}
