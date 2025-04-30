package com.quizplatform.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * OpenAPI 문서화 설정 클래스 (간소화 버전)
 */
@Configuration
public class SpringDocConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringDocConfig.class);

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * API Gateway OpenAPI 설정
     */
    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quiz Platform API")
                        .description("Quiz Platform 마이크로서비스 API 문서")
                        .version("v0.1.0")
                        .contact(new Contact()
                                .name("채기훈")
                                .email("deokdory@gmail.com")
                                .url("https://github.com/deokdory"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("로컬 개발 서버")));
    }

    // 오류가 발생하는 Bean 제거 - SwaggerConfigController로 대체
}
