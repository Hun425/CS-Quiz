package com.quizplatform.apigateway.config; // 실제 패키지 경로 확인

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Configuration
public class SwaggerGroupConfig {

    private static final Logger log = LoggerFactory.getLogger(SwaggerGroupConfig.class);

    @Bean
    @Lazy(false)
    public List<GroupedOpenApi> apis(RouteDefinitionLocator locator) {
        List<GroupedOpenApi> groups = new ArrayList<>();
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();

        if (definitions == null) {
            log.warn("Route definitions could not be loaded from RouteDefinitionLocator.");
            return groups;
        }

        log.info("Dynamically creating GroupedOpenApi beans from {} route definitions...", definitions.size());
        
        // 주요 서비스 ID와 경로를 매핑
        Map<String, String> servicePathMapping = new HashMap<>();
        
        // 모든 라우트를 순회하면서 디버깅
        for (RouteDefinition route : definitions) {
            log.debug("Examining route: id={}, uri={}, predicates={}", 
                      route.getId(), route.getUri(), route.getPredicates());
        }

        // OpenAPI 문서 라우트 검색 ("-openapi"로 끝나는 ID)
        definitions.stream()
            .filter(routeDefinition -> routeDefinition.getId() != null && routeDefinition.getId().endsWith("-openapi"))
            .forEach(routeDefinition -> {
                String serviceId = routeDefinition.getId().replace("-openapi", "-service");
                
                // Path Predicate 찾기
                Optional<String> pathPredicate = routeDefinition.getPredicates().stream()
                        .filter(pd -> "Path".equalsIgnoreCase(pd.getName()))
                        .flatMap(pd -> pd.getArgs().values().stream())
                        .findFirst();
                
                if (pathPredicate.isPresent()) {
                    String path = pathPredicate.get();
                    log.debug("Found OpenAPI route: id={}, path={}", routeDefinition.getId(), path);
                    
                    // 서비스 이름 추출 (예: /v3/api-docs/users -> users)
                    String serviceName = path.replaceAll("/v3/api-docs/", "");
                    
                    // 명시적으로 그룹 생성
                    groups.add(GroupedOpenApi.builder()
                            .group(serviceId)
                            .pathsToMatch("/api/" + serviceName + "/**") // 실제 API 경로와 일치
                            .build());
                }
            });

        // 서비스 라우트 처리 ("-service"로 끝나는 ID)
        definitions.stream()
            .filter(routeDefinition -> routeDefinition.getId() != null && routeDefinition.getId().endsWith("-service"))
            .forEach(routeDefinition -> {
                String serviceId = routeDefinition.getId();
                
                // Path Predicate 찾기
                Optional<String> pathPredicate = routeDefinition.getPredicates().stream()
                        .filter(pd -> "Path".equalsIgnoreCase(pd.getName()))
                        .flatMap(pd -> pd.getArgs().values().stream())
                        .findFirst();
                
                if (pathPredicate.isPresent()) {
                    String originalPath = pathPredicate.get();
                    log.info("Creating GroupedOpenApi for route: id={}, originalPath={}", serviceId, originalPath);
                    
                    // 원래 경로 그대로 사용 (API Gateway 라우팅 설정과 일치)
                    if (!groups.stream().anyMatch(api -> api.getGroup().equals(serviceId))) {
                        groups.add(GroupedOpenApi.builder()
                                .pathsToMatch(originalPath)
                                .group(serviceId)
                                .build());
                    }
                } else {
                    log.warn("No Path predicate found for route: {}", serviceId);
                }
            });
        
        log.info("Successfully created {} GroupedOpenApi beans.", groups.size());
        return groups;
    }
} 