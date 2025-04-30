package com.quizplatform.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Swagger 설정 디버깅을 위한 컨트롤러
 * 개발 환경에서만 사용하고 프로덕션에서는 비활성화하는 것을 권장
 */
@RestController
public class SwaggerDebugController {

    private static final Logger log = LoggerFactory.getLogger(SwaggerDebugController.class);
    
    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;
    
    /**
     * 현재 등록된 모든 라우트 정의를 반환합니다.
     */
    @GetMapping("/api/debug/routes")
    public ResponseEntity<List<RouteDefinition>> getAllRoutes() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collect(Collectors.toList())
                .block();
        
        log.info("Total routes: {}", routes != null ? routes.size() : 0);
        return ResponseEntity.ok(routes);
    }
    
    /**
     * Swagger UI가 사용할 수 있는 형식으로 API 문서 URL 목록을 반환합니다.
     */
    @GetMapping("/api/debug/swagger-urls")
    public ResponseEntity<Map<String, Object>> getSwaggerUrls() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collect(Collectors.toList())
                .block();
        
        if (routes == null) {
            log.warn("라우트 정의를 로드할 수 없습니다.");
            return ResponseEntity.ok(Map.of("error", "라우트 정의를 로드할 수 없습니다."));
        }
        
        // OpenAPI 문서 라우트만 필터링 (-openapi로 끝나는 ID)
        List<Map<String, String>> urls = routes.stream()
                .filter(route -> route.getId() != null && route.getId().endsWith("-openapi"))
                .map(route -> {
                    String path = route.getPredicates().stream()
                            .filter(pd -> "Path".equalsIgnoreCase(pd.getName()))
                            .flatMap(pd -> pd.getArgs().values().stream())
                            .findFirst()
                            .orElse("");
                    
                    // 서비스 이름 가져오기 (예: user-openapi -> User Service)
                    String name = route.getId().replace("-openapi", "");
                    name = name.substring(0, 1).toUpperCase() + name.substring(1) + " Service";
                    
                    Map<String, String> urlInfo = new HashMap<>();
                    urlInfo.put("url", path); // 예: /v3/api-docs/users
                    urlInfo.put("name", name); // 예: User Service
                    
                    log.info("Added Swagger URL: {}", urlInfo);
                    return urlInfo;
                })
                .collect(Collectors.toList());
        
        // API Gateway 자체 문서 추가
        Map<String, String> gatewayUrlInfo = new HashMap<>();
        gatewayUrlInfo.put("url", "/v3/api-docs/api-gateway");
        gatewayUrlInfo.put("name", "API Gateway");
        urls.add(0, gatewayUrlInfo); // 목록 맨 앞에 추가
        
        Map<String, Object> response = new HashMap<>();
        response.put("urls", urls);
        response.put("urls.primaryName", "API Gateway");
        
        return ResponseEntity.ok(response);
    }
} 