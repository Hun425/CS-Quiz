package com.quizplatform.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 서비스 디스커버리를 위한 Eureka 서버
 * 마이크로서비스 아키텍처에서 서비스 등록 및 검색을 담당
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
} 