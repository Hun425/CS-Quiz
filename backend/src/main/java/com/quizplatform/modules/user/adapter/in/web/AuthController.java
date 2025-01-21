package com.quizplatform.modules.user.adapter.in.web;

import com.quizplatform.modules.user.application.UserService;
import com.quizplatform.modules.user.application.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "소셜 로그인 및 인증 관련 API")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "소셜 로그인", description = "소셜 미디어 제공자를 통한 OAuth2 로그인을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/oauth2/callback/{provider}")
    public ResponseEntity<AuthResponse> oauthCallback(
            @Parameter(description = "소셜 로그인 제공자 (GOOGLE, GITHUB, KAKAO)", required = true)
            @PathVariable String provider,
            @Parameter(description = "OAuth2 인증 코드", required = true)
            @RequestParam String code
    ) {
        return ResponseEntity.ok(authService.socialLogin(provider, code));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "리프레시 토큰 (Bearer 형식)", required = true)
            @RequestHeader("Authorization") String refreshToken
    ) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}

