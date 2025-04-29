package com.quizplatform.apigateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

/**
 * SpringDoc과 Swagger UI의 통합 설정을 담당하는 클래스
 * 시큐리티와 레이트 리미팅이 제거된 간소화된 버전
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Configuration
public class SpringDocConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 각 마이크로서비스의 API 문서를 Swagger UI에 통합하는 설정
     * @param swaggerUiConfig Swagger UI 설정 속성
     * @return 설정된 SwaggerUiConfigParameters 객체
     */
    @Primary
    @Bean
    public SwaggerUiConfigParameters swaggerUiConfigParameters(
            SwaggerUiConfigProperties swaggerUiConfig) {
        
        // 기존 설정된 URL 목록 가져오기 (또는 새로운 Set 생성)
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();
        
        // API Gateway 자체 문서 URL 추가
        AbstractSwaggerUiConfigProperties.SwaggerUrl gatewayUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        gatewayUrl.setName("API Gateway");
        gatewayUrl.setUrl("/v3/api-docs/api-gateway");
        urls.add(gatewayUrl);
        
        // 사용자 서비스 API 문서 URL 추가
        AbstractSwaggerUiConfigProperties.SwaggerUrl userUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        userUrl.setName("User Service");
        userUrl.setUrl("/v3/api-docs/users");
        urls.add(userUrl);
        
        // 퀴즈 서비스 API 문서 URL 추가
        AbstractSwaggerUiConfigProperties.SwaggerUrl quizUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        quizUrl.setName("Quiz Service");
        quizUrl.setUrl("/v3/api-docs/quizzes");
        urls.add(quizUrl);
        
        // 배틀 서비스 API 문서 URL 추가
        AbstractSwaggerUiConfigProperties.SwaggerUrl battleUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        battleUrl.setName("Battle Service");
        battleUrl.setUrl("/v3/api-docs/battles");
        urls.add(battleUrl);
        
        // 설정된 URL 목록 적용
        swaggerUiConfig.setUrls(urls);
        
        // SwaggerUiConfigParameters 객체 생성 및 반환
        return new SwaggerUiConfigParameters(swaggerUiConfig);
    }
}
