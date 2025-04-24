package com.quizplatform.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API 게이트웨이의 Swagger(OpenAPI) 문서화 설정 클래스
 */
@Configuration
public class SwaggerConfig {

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
                        new Server().url("/").description("게이트웨이 URL")
                ));
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
