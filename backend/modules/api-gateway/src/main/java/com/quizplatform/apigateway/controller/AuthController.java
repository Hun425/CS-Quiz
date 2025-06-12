package com.quizplatform.apigateway.controller;

import com.quizplatform.apigateway.dto.*;
import com.quizplatform.apigateway.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication API", description = "인증 관련 API")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.email());
        
        return authService.login(loginRequest)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Login successful for email: {}", loginRequest.email()))
                .doOnError(error -> log.error("Login failed for email: {}", loginRequest.email(), error))
                .onErrorReturn(ResponseEntity.status(401).build());
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    public Mono<ResponseEntity<RefreshTokenResponse>> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        
        return authService.refreshToken(request)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Token refresh successful"))
                .doOnError(error -> log.error("Token refresh failed", error))
                .onErrorReturn(ResponseEntity.status(401).build());
    }
    
    @PostMapping("/oauth2/callback")
    @Operation(summary = "OAuth2 콜백", description = "OAuth2 인증 후 콜백을 처리하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OAuth2 인증 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "OAuth2 인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public Mono<ResponseEntity<LoginResponse>> oauth2Callback(@RequestBody @Valid OAuth2CallbackRequest request) {
        log.info("OAuth2 callback request for provider: {}", request.provider());
        
        return authService.oauth2Login(request)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("OAuth2 login successful for provider: {}", request.provider()))
                .doOnError(error -> log.error("OAuth2 login failed for provider: {}", request.provider(), error))
                .onErrorReturn(ResponseEntity.status(401).build());
    }
    
    @GetMapping("/health")
    @Operation(summary = "인증 서비스 헬스체크", description = "인증 서비스의 상태를 확인합니다.")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("Auth service is healthy"));
    }
}