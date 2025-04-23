package com.quizplatform.battle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 대전 서비스 애플리케이션 진입점
 * 실시간 퀴즈 대전 기능 담당
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BattleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BattleApplication.class, args);
    }
} 