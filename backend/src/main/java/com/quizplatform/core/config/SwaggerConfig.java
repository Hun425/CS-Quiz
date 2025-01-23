package com.quizplatform.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 보안 스키마 이름 상수 정의
        final String securitySchemeName = "bearerAuth";

        // API 기본 정보 설정
        Info info = new Info()
                .title("Quiz Platform API")
                .version("v1.0.0")
                .description("대중교통 이용자를 위한 CS 퀴즈 플랫폼 API 문서")
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://springdoc.org"));

        // JWT 인증 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // API 문서에 보안 요구사항 추가
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);

        // OpenAPI 객체 생성 및 반환 (버전 명시적 지정)
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme))
                .openapi("3.0.1");  // OpenAPI 버전을 명시적으로 지정
    }
}