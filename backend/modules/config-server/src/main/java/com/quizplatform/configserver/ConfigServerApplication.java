package com.quizplatform.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config 서버 애플리케이션
 */
@SpringBootApplication(
        scanBasePackages = {"com.quizplatform.configserver", "com.quizplatform.common"},
        exclude = {DataSourceAutoConfiguration.class}
)
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
