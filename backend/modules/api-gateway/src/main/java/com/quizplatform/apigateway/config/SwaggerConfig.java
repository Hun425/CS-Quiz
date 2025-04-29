package com.quizplatform.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API 게이트웨이의 Swagger(OpenAPI) 문서화 설정 클래스
 * 시큐리티 관련 설정이 제거된 버전
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * API 게이트웨이 OpenAPI 기본 설정
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("퀴즈 플랫폼 API 게이트웨이")
                        .description("모듈식 아키텍처의 API 게이트웨이 및 통합 API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("퀴즈 플랫폼 개발팀")
                                .email("support@quizplatform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("").description("게이트웨이 URL"),
                        new Server().url("/api/users").description("사용자 서비스 API"),
                        new Server().url("/api/quizzes").description("퀴즈 서비스 API"),
                        new Server().url("/api/battles").description("배틀 서비스 API")
                ));
    }
    
    /**
     * API Gateway 자체 API 그룹 설정
     */
    @Bean
    public GroupedOpenApi gatewayApiGroup() {
        return GroupedOpenApi.builder()
                .group("api-gateway")
                .pathsToMatch("/api/gateway/**")
                .build();
    }
    
    /**
     * 인증 관련 API 그룹 설정
     */
    @Bean
    public GroupedOpenApi authApiGroup() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**")
                .build();
    }
    
    /**
     * 사용자 관련 API 그룹 설정
     */
    @Bean
    public GroupedOpenApi userApiGroup() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/users/**")
                .build();
    }
    
    /**
     * 퀴즈 관련 API 그룹 설정
     */
    @Bean
    public GroupedOpenApi quizApiGroup() {
        return GroupedOpenApi.builder()
                .group("quizzes")
                .pathsToMatch("/api/quizzes/**")
                .build();
    }
    
    /**
     * 배틀 관련 API 그룹 설정
     */
    @Bean
    public GroupedOpenApi battleApiGroup() {
        return GroupedOpenApi.builder()
                .group("battles")
                .pathsToMatch("/api/battles/**")
                .build();
    }
}
