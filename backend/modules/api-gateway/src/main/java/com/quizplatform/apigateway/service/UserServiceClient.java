package com.quizplatform.apigateway.service;

// OAuth2 전용으로 변경하여 LoginRequest import 제거
import com.quizplatform.apigateway.dto.OAuth2UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    // OAuth2 전용 로그인으로 전환하여 일반 인증 메서드는 제거
    
    /**
     * 사용자 ID로 사용자 정보 조회
     */
    public Mono<UserAuthInfo> getUserById(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("lb://user-service/auth/user/{userId}", userId)
                .retrieve()
                .bodyToMono(UserAuthInfo.class)
                .doOnSuccess(user -> log.info("User info retrieved: {}", user.email()))
                .doOnError(error -> log.error("Failed to get user info: {}", error.getMessage()));
    }
    
    /**
     * OAuth2 사용자 정보 처리 (조회/생성)
     */
    public Mono<UserAuthInfo> processOAuth2User(OAuth2UserRequest request) {
        return webClientBuilder.build()
                .post()
                .uri("lb://user-service/auth/oauth2")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserAuthInfo.class)
                .doOnSuccess(user -> log.info("OAuth2 user processed successfully: {}", user.email()))
                .doOnError(error -> log.error("OAuth2 user processing failed: {}", error.getMessage()));
    }
    
    /**
     * 사용자 인증 정보를 나타내는 record
     */
    public record UserAuthInfo(
        Long id,
        String email,
        String displayName,
        List<String> roles
    ) {}
}