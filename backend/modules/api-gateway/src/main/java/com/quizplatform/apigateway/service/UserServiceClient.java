package com.quizplatform.apigateway.service;

import com.quizplatform.apigateway.dto.LoginRequest;
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
    
    /**
     * 사용자 인증 (이메일/비밀번호)
     */
    public Mono<UserAuthInfo> authenticateUser(LoginRequest loginRequest) {
        return webClientBuilder.build()
                .post()
                .uri("lb://user-service/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(UserAuthInfo.class)
                .doOnSuccess(user -> log.info("User authenticated successfully: {}", user.email()))
                .doOnError(error -> log.error("User authentication failed: {}", error.getMessage()));
    }
    
    /**
     * 사용자 ID로 사용자 정보 조회
     */
    public Mono<UserAuthInfo> getUserById(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("lb://user-service/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserAuthInfo.class)
                .doOnSuccess(user -> log.info("User info retrieved: {}", user.email()))
                .doOnError(error -> log.error("Failed to get user info: {}", error.getMessage()));
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