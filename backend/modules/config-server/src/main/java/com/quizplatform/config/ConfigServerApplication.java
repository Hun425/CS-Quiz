package com.quizplatform.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * 설정 중앙화를 위한 Spring Cloud Config Server
 * 마이크로서비스 아키텍처에서 중앙 집중식 설정 관리 담당
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
} 