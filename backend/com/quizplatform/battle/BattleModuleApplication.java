package com.quizplatform.battle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Battle 모듈의 메인 애플리케이션 클래스
 * 헥사고날 아키텍처 기반의 Battle 모듈의 시작점 역할
 */
@SpringBootApplication
public class BattleModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BattleModuleApplication.class, args);
    }
}
