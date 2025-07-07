package com.quizplatform.core.controller.auth;

import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.dto.AuthResponse;


import com.quizplatform.core.service.user.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2 인증", description = "소셜 로그인 관련 API")
public class OAuth2Controller {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @Operation(summary = "소셜 로그인 시작", description = "선택한 제공자(Google, Github, Kakao)를 통한 소셜 로그인을 시작합니다.")
    @GetMapping("/authorize/{provider}")
    public ResponseEntity<Void> startOAuth2Login(@PathVariable @Parameter(description = "소셜 로그인 제공자 (google, github, kakao)") String provider, HttpServletRequest request) {

        // 인증 URL 생성
        String authorizationUri = authService.getAuthorizationUrl(provider);

        // 인증 페이지로 리다이렉트
        return ResponseEntity.status(302).location(URI.create(authorizationUri)).build();
    }

    // OAuth2 콜백은 Spring Security에서 자동으로 처리하므로 이 메소드는 제거합니다.
    // OAuth2SuccessHandler에서 모든 콜백 처리를 담당합니다.

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("X-Refresh-Token") String refreshToken) {
        log.info("리프레시 토큰 요청 수신: {}", refreshToken);
        
        // Bearer 접두사 제거
        if (refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        
        // 리프레시 토큰 검증 및 새 토큰 발급
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        log.info("새로운 액세스 토큰 발급 완료: {}", authResponse.getAccessToken());
        return ResponseEntity.ok(authResponse);
    }
}