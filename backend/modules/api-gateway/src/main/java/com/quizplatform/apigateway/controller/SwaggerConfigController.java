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
 * Swagger 설정 엔드포인트를 커스터마이징하는 컨트롤러
 */
@RestController
public class SwaggerConfigController {

    private static final Logger log = LoggerFactory.getLogger(SwaggerConfigController.class);
    
    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;
    
    /**
     * '/v3/api-docs/swagger-config' 엔드포인트를 커스터마이징합니다.
     * SpringDoc의 기본 엔드포인트를 오버라이드합니다.
     */
    @GetMapping("/v3/api-docs/swagger-config")
    public ResponseEntity<Map<String, Object>> swaggerConfig() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collect(Collectors.toList())
                .block();
        
        if (routes == null) {
            log.warn("라우트 정의를 로드할 수 없습니다.");
            return ResponseEntity.ok(createEmptyConfig());
        }
        
        log.info("Swagger 설정 생성 중: 총 {} 라우트", routes.size());
        
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
                    
                    log.info("Swagger URL 추가: {} -> {}", name, path);
                    return urlInfo;
                })
                .collect(Collectors.toList());
        
        // API Gateway 자체 문서 추가
        Map<String, String> gatewayUrlInfo = new HashMap<>();
        gatewayUrlInfo.put("url", "/v3/api-docs/api-gateway");
        gatewayUrlInfo.put("name", "API Gateway");
        urls.add(0, gatewayUrlInfo); // 목록 맨 앞에 추가
        
        Map<String, Object> config = createEmptyConfig();
        config.put("urls", urls);
        
        if (!urls.isEmpty()) {
            config.put("urls.primaryName", "API Gateway");
        }
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * 기본 Swagger 설정을 생성합니다.
     */
    private Map<String, Object> createEmptyConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("configUrl", "/v3/api-docs/swagger-config");
        config.put("oauth2RedirectUrl", "/webjars/swagger-ui/oauth2-redirect.html");
        config.put("url", "/v3/api-docs");
        config.put("validatorUrl", "");
        return config;
    }
} 