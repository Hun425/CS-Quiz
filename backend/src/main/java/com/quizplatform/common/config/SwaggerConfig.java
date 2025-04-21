package com.quizplatform.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 설정 클래스
 * 
 * <p>API 문서화를 위한 Swagger/OpenAPI 설정을 담당합니다.
 * JWT 인증을 포함한 API 문서의 기본 정보 및 보안 설정을 제공합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 17
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    /**
     * OpenAPI 설정 빈
     * 
     * <p>API 문서의 기본 정보, 보안 스키마, 사용법 설명 등을 정의합니다.</p>
     * 
     * @return 구성된 OpenAPI 객체
     */
    @Bean
    public OpenAPI openAPI() {
        // 보안 스키마 이름 상수 정의
        final String jwtSecuritySchemeName = "bearerAuth";

        // API 기본 정보 설정
        Info info = new Info()
                .title("Quiz Platform API")
                .version("v1.0.0")
                .description("대중교통 이용자를 위한 CS 퀴즈 플랫폼 API 문서\n\n" +
                        "테스트 방법:\n" +
                        "1. '/api/test-auth/token' 엔드포인트를 사용하여 테스트 JWT 토큰을 발급받습니다.\n" +
                        "2. 'Authorize' 버튼을 클릭합니다.\n" +
                        "3. 발급받은 JWT 토큰을 입력하고 인증합니다. (형식: Bearer your_token_here)\n" +
                        "4. API를 테스트합니다.\n\n" +
                        "주의: OAuth2/소셜 로그인은 Swagger UI에서 직접 테스트할 수 없습니다. 대신 위의 테스트 토큰을 사용하세요.")
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://springdoc.org"));

        // JWT 인증 설정
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name(jwtSecuritySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // API 문서에 보안 요구사항 추가
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSecuritySchemeName);

        // OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(jwtSecuritySchemeName, jwtSecurityScheme))
                .openapi("3.0.1");
    }
}