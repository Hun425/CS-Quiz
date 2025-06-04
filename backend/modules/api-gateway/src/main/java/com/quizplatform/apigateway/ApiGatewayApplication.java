package com.quizplatform.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway 서비스 애플리케이션 진입점
 */
@SpringBootApplication(scanBasePackages = {"com.quizplatform.apigateway", "com.quizplatform.common"})
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
