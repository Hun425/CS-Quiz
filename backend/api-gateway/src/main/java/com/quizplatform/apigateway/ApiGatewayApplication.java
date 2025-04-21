package com.quizplatform.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * API 게이트웨이 애플리케이션 메인 클래스
 * 
 * <p>퀴즈 플랫폼의 API 게이트웨이를 시작하는 진입점입니다.</p>
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.quizplatform.apigateway",
    "com.quizplatform.core",
    "com.quizplatform.modules.user",
    "com.quizplatform.modules.quiz",
    "com.quizplatform.modules.tag",
    "com.quizplatform.modules.battle",
    "com.quizplatform.modules.search"
})
public class ApiGatewayApplication {
    
    /**
     * 애플리케이션 시작 메서드
     * 
     * @param args 명령행 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}