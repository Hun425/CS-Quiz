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
import java.util.Optional;

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

        definitions.stream()
            // '-service'로 끝나는 ID를 가진 라우트만 필터링 (자동화를 위해)
            .filter(routeDefinition -> routeDefinition.getId() != null && routeDefinition.getId().matches(".*-service"))
            .forEach(routeDefinition -> {
                String serviceId = routeDefinition.getId();
                // 라우트 정의에서 Path Predicate를 찾아 경로 패턴 추출 시도
                Optional<String> pathPredicate = routeDefinition.getPredicates().stream()
                        .filter(pd -> "Path".equalsIgnoreCase(pd.getName()))
                        .flatMap(pd -> pd.getArgs().values().stream())
                        .findFirst();

                // Path Predicate가 있으면 해당 패턴 사용, 없으면 기본 패턴 사용 (예: /serviceId/**)
                // 현재 설정에서는 /api/users/** 등을 사용하므로 pathPredicate 사용이 적절
                String pathPattern = pathPredicate.orElse("/" + serviceId.replace("-service", "") + "/**"); // 기본값 fallback

                log.info("Creating GroupedOpenApi for route: id={}, pathPattern={}", serviceId, pathPattern);
                groups.add(GroupedOpenApi.builder()
                        // `pathsToMatch`는 게이트웨이로 들어오는 요청 경로 기준이어야 함
                        .pathsToMatch(pathPattern)
                        .group(serviceId) // 그룹 이름은 서비스 ID 그대로 사용
                        .build());
            });

        // 게이트웨이 자체 API 그룹 추가 (필요하다면)
        // 주의: 게이트웨이 자체 API를 위한 라우트가 application.yml에 정의되어 있어야 함
        // 예: id: api-gateway-internal, uri: http://localhost:${server.port}, predicates: Path=/api/internal/**
        // definitions.stream()
        //     .filter(routeDefinition -> "api-gateway-internal".equals(routeDefinition.getId())) // 게이트웨이 라우트 ID
        //     .findFirst()
        //     .ifPresent(routeDefinition -> {
        //         Optional<String> pathPredicate = routeDefinition.getPredicates().stream()
        //                 .filter(pd -> "Path".equalsIgnoreCase(pd.getName()))
        //                 .flatMap(pd -> pd.getArgs().values().stream())
        //                 .findFirst();
        //         String pathPattern = pathPredicate.orElse("/api/internal/**"); // 기본값
        //         log.info("Creating GroupedOpenApi for gateway itself: id={}, pathPattern={}", routeDefinition.getId(), pathPattern);
        //         groups.add(GroupedOpenApi.builder()
        //                 .pathsToMatch(pathPattern)
        //                 .group("api-gateway") // 그룹 이름
        //                 .build());
        //     });


        log.info("Successfully created {} GroupedOpenApi beans.", groups.size());
        return groups;
    }
} 