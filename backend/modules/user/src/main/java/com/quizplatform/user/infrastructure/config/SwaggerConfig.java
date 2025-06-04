package com.quizplatform.user.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger(OpenAPI) 문서화 설정 클래스
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * OpenAPI 기본 설정
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI userOpenAPI() {
        final String schemeName = "bearerAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .name(schemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(schemeName);

        return new OpenAPI()
                .info(new Info()
                        .title("사용자 모듈 API")
                        .description("사용자 관리 및 인증 기능을 제공하는 REST API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("퀴즈 플랫폼 개발팀")
                                .email("support@quizplatform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes(schemeName, securityScheme))
                .servers(List.of(
                        // API Gateway를 통한 경로 추가
                        new Server().url("/api/users").description("API Gateway 경로"),
                        // 직접 접근 경로
                        new Server().url("/").description("직접 접근 URL")
                ));
    }
    
    /**
     * PathSelectors 설정으로 원하는 API 경로만 문서화
     * Security 설정을 추가하여 인증 정보를 노출
     */
    /*@Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-api")
                .pathsToMatch("/**")
                .build();
    }*/
}
