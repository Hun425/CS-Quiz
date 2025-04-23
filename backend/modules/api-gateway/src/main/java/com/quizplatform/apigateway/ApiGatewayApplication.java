package com.quizplatform.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 게이트웨이 애플리케이션 진입점
 * 모든 마이크로서비스의 단일 진입점 역할을 함
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
