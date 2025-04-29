package com.quizplatform.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

/**
 * Swagger 라우팅을 관리하는 컨트롤러
 * API Gateway 내에서 마이크로서비스의 API 문서 접근을 돕는 유틸리티 메서드 제공
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@RestController
public class SwaggerRoutingController {

    @Autowired
    private DiscoveryClient discoveryClient;

    private final WebClient.Builder webClientBuilder;

    public SwaggerRoutingController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 사용 가능한 서비스 목록 반환
     * @return 서비스 ID 목록
     */
    @GetMapping("/api/services")
    public Mono<List<String>> getServices() {
        return Mono.just(discoveryClient.getServices());
    }
    
    /**
     * Swagger UI 루트 경로 리디렉션
     * @param response 서버 HTTP 응답
     * @return 리디렉션 응답
     */
    @GetMapping("/")
    public Mono<Void> swaggerUiRedirect(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(URI.create("/swagger-ui.html"));
        return response.setComplete();
    }
}
