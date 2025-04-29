package com.quizplatform.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SpringDoc과 Swagger UI의 통합 설정을 담당하는 클래스
 * API Gateway에서 각 마이크로서비스의 API 문서를 통합하여 표시
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
     * RouteDefinitionLocator를 통해 모든 서비스 라우트를 동적으로 발견하고
     * Swagger UI에 표시할 URL 목록 생성
     *
     * @param swaggerUiConfig Swagger UI 설정 속성
     * @param routeLocator 라우트 정의 로케이터
     * @return 설정된 SwaggerUiConfigParameters 객체
     */
    @Primary
    @Bean
    public SwaggerUiConfigParameters swaggerUiConfigParameters(
            SwaggerUiConfigProperties swaggerUiConfig,
            RouteDefinitionLocator routeLocator) {
        
        // 기존 설정된 URL 목록 가져오기 (또는 새로운 Set 생성)
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();
        
        // API Gateway 자체 문서 URL 추가
        AbstractSwaggerUiConfigProperties.SwaggerUrl gatewayUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        gatewayUrl.setName("API Gateway");
        gatewayUrl.setUrl("/v3/api-docs/api-gateway");
        urls.add(gatewayUrl);
        
        // 각 마이크로서비스 API 문서 URL 추가
        List<RouteDefinition> definitions = routeLocator.getRouteDefinitions()
                .collectList().block();
        
        if (definitions != null) {
            definitions.stream()
                    .filter(routeDefinition -> routeDefinition.getId().matches(".*-service$"))
                    .forEach(routeDefinition -> {
                        String name = routeDefinition.getId().replaceAll("-service", "");
                        String displayName = name.substring(0, 1).toUpperCase() + name.substring(1) + " Service";
                        String url = "/v3/api-docs/" + name;
                        
                        // 새 URL 객체 생성 및 추가
                        AbstractSwaggerUiConfigProperties.SwaggerUrl serviceUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
                        serviceUrl.setName(displayName);
                        serviceUrl.setUrl(url);
                        urls.add(serviceUrl);
                    });
        }
        
        // 설정된 URL 목록 적용
        swaggerUiConfig.setUrls(urls);
        
        // SwaggerUiConfigParameters 객체 생성 및 반환
        return new SwaggerUiConfigParameters(swaggerUiConfig);
    }
    
    /** 
     * SwaggerConfig 클래스와의 충돌을 피하기 위해 주석 처리
     * 또는 그룹 이름을 다르게 설정
     */
    /*
    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("gateway")
                .pathsToMatch("/api/gateway/**")
                .build();
    }
    */
}
