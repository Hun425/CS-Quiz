package com.quizplatform.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

/**
 * API 게이트웨이의 Swagger(OpenAPI) 문서화 설정 클래스
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
        // 보안 스키마 이름 상수 정의
        final String jwtSecuritySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("퀴즈 플랫폼 API 게이트웨이")
                        .description("모듈식 아키텍처의 API 게이트웨이 및 통합 API 문서\n\n" +
                                "테스트 방법:\n" +
                                "1. '/api/auth/test-token' 엔드포인트를 사용하여 테스트 JWT 토큰을 발급받습니다.\n" +
                                "2. 'Authorize' 버튼을 클릭합니다.\n" +
                                "3. 발급받은 JWT 토큰을 입력하고 인증합니다. (형식: Bearer your_token_here)\n" +
                                "4. API를 테스트합니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("퀴즈 플랫폼 개발팀")
                                .email("support@quizplatform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("/").description("게이트웨이 URL"),
                        new Server().url("/api/users").description("사용자 서비스 API"),
                        new Server().url("/api/quizzes").description("퀴즈 서비스 API"),
                        new Server().url("/api/battles").description("배틀 서비스 API")
                ))
                .addSecurityItem(new SecurityRequirement().addList(jwtSecuritySchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSecuritySchemeName, new SecurityScheme()
                                .name(jwtSecuritySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
    
    @Bean
    @Lazy(false)
    public List<GroupedOpenApi> apis(SwaggerUiConfigParameters swaggerUiConfigParameters,
                                     SpringDocConfigProperties springDocConfigProperties,
                                     RouteDefinitionLocator routeDefinitionLocator) {
        List<RouteDefinition> definitions = routeDefinitionLocator.getRouteDefinitions().collectList().block();
        List<GroupedOpenApi> groups = new ArrayList<>();
        
        definitions.stream()
                .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
                .forEach(routeDefinition -> {
                    String name = routeDefinition.getId().replaceAll("-service", "");
                    swaggerUiConfigParameters.addGroup(name);
                    GroupedOpenApi.builder()
                            .pathsToMatch("/" + name + "/**")
                            .group(name)
                            .build();
                });
                
        return groups;
    }

    /**
     * Swagger UI 리소스를 서빙하기 위한 라우터 함수
     */
    @Bean
    public RouterFunction<ServerResponse> swaggerUiRouterFunction(ResourceLoader resourceLoader) {
        try {
            Resource indexHtml = resourceLoader.getResource("classpath:META-INF/resources/webjars/swagger-ui/index.html");
            
            return RouterFunctions
                .route(GET("/").and(accept(MediaType.TEXT_HTML)), 
                    req -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml))
                .andRoute(GET("/swagger-ui.html").and(accept(MediaType.TEXT_HTML)), 
                    req -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
        } catch (Exception e) {
            // 개발 중에만 로그 남기기
            e.printStackTrace();
            return RouterFunctions.route(GET("/**"), request -> ServerResponse.notFound().build());
        }
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
