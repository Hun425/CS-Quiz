package com.quizplatform.quiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 퀴즈 서비스 애플리케이션 진입점
 * 퀴즈 생성, 조회, 응시 기능 담당
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@SpringBootApplication(scanBasePackages = {"com.quizplatform.quiz", "com.quizplatform.common"})
@EnableDiscoveryClient
public class QuizApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizApplication.class, args);
    }
} 