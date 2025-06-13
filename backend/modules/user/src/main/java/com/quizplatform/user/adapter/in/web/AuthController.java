package com.quizplatform.user.adapter.in.web;

import com.quizplatform.user.adapter.in.web.dto.AuthLoginRequest;
import com.quizplatform.user.adapter.in.web.dto.AuthUserResponse;
import com.quizplatform.user.adapter.in.web.dto.OAuth2UserRequest;
import com.quizplatform.user.adapter.in.web.dto.RegisterRequest;
import com.quizplatform.user.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "사용자 인증 API")
public class AuthController {
    
    private final AuthService authService;
    
    // OAuth2 전용 로그인으로 전환하여 일반 로그인/회원가입 엔드포인트는 제거
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 인증용 사용자 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<AuthUserResponse> getUserById(@PathVariable Long userId) {
        log.debug("Get user by ID request: {}", userId);
        AuthUserResponse response = authService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/oauth2")
    @Operation(summary = "OAuth2 사용자 처리", description = "OAuth2 인증으로 받은 사용자 정보를 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OAuth2 사용자 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    public ResponseEntity<AuthUserResponse> processOAuth2User(@RequestBody @Valid OAuth2UserRequest request) {
        log.info("OAuth2 user processing request for email: {} from provider: {}", request.email(), request.provider());
        AuthUserResponse response = authService.processOAuth2User(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "헬스체크", description = "인증 서비스의 상태를 확인합니다.")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is healthy");
    }
}