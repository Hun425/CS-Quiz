package com.quizplatform.core.controller.auth;

import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.dto.AuthResponse;
import com.quizplatform.core.service.AuthService;
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

    @Operation(summary = "OAuth2 콜백 처리", description = "소셜 로그인 인증 후 콜백을 처리하고 JWT 토큰을 발급합니다.")
    @GetMapping("/callback/{provider}")
    public void oauth2Callback(@PathVariable String provider,
                               @RequestParam String code,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        // OAuth2 인증 처리 및 JWT 토큰 발급
        AuthResponse authResponse = authService.processOAuth2Login(provider, code);

        // 프론트엔드 리다이렉트 URI 생성
        String targetUrl = UriComponentsBuilder.fromUriString(authService.getAuthorizedRedirectUri())
                .queryParam("token", authResponse.getAccessToken())
                .queryParam("refreshToken", authResponse.getRefreshToken())
                .queryParam("email", authResponse.getEmail())
                .queryParam("username", authResponse.getUsername())
                .queryParam("expiresIn", authResponse.getExpiresIn())
                .build().toUriString();

        // 프론트엔드로 리다이렉트
        response.sendRedirect(targetUrl);
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {

        // 리프레시 토큰 검증 및 새 토큰 발급
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }
}