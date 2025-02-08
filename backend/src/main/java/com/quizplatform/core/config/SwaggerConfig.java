package com.quizplatform.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${app.oauth2.authorized-redirect-uri}")
    private String redirectUri;

    @Bean
    public OpenAPI openAPI() {
        // 보안 스키마 이름 상수 정의
        final String jwtSecuritySchemeName = "bearerAuth";
        final String oauth2SecuritySchemeName = "oauth2";

        // API 기본 정보 설정
        Info info = new Info()
                .title("Quiz Platform API")
                .version("v1.0.0")
                .description("대중교통 이용자를 위한 CS 퀴즈 플랫폼 API 문서\n\n" +
                        "소셜 로그인 테스트 방법:\n" +
                        "1. 'Authorize' 버튼을 클릭합니다.\n" +
                        "2. OAuth2 인증 방식을 선택합니다.\n" +
                        "3. 원하는 소셜 로그인 제공자(Google, Github, Kakao)를 선택합니다.\n" +
                        "4. 로그인 후 발급받은 토큰으로 API를 테스트합니다.")
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://springdoc.org"));

        // JWT 인증 설정
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name(jwtSecuritySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // OAuth2 인증 설정
        SecurityScheme oauth2SecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("/api/oauth2/authorize")
                                .tokenUrl("/api/oauth2/token")
                                .scopes(new Scopes()
                                        .addString("read", "읽기 권한")
                                        .addString("write", "쓰기 권한"))
                        ));

        // API 문서에 보안 요구사항 추가
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSecuritySchemeName)
                .addList(oauth2SecuritySchemeName);

        // OpenAPI 객체 생성 및 반환
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes(jwtSecuritySchemeName, jwtSecurityScheme)
                        .addSecuritySchemes(oauth2SecuritySchemeName, oauth2SecurityScheme))
                .openapi("3.0.1");
    }
}