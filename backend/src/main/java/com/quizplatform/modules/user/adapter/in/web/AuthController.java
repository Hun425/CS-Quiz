package com.quizplatform.core.controller;

import com.quizplatform.core.config.security.jwt.JwtTokenProvider;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.dto.AuthResponse;
import com.quizplatform.core.dto.UserResponse;
import com.quizplatform.core.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "소셜 로그인 및 인증 관련 API")
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    @Operation(summary = "소셜 로그인 URL 조회",
            description = "특정 소셜 미디어 제공자의 OAuth2 로그인 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL 조회 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 제공자 이름")
    })
    @GetMapping("/oauth2/url/{provider}")
    public ResponseEntity<String> getOAuth2LoginUrl(
            @Parameter(description = "소셜 로그인 제공자 (GOOGLE, GITHUB, KAKAO)", required = true)
            @PathVariable String provider) {
        AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());
        String loginUrl = switch (authProvider) {
            case GOOGLE -> "https://accounts.google.com/o/oauth2/v2/auth?client_id=${client-id}&response_type=code&scope=email%20profile&redirect_uri=${redirect-uri}";
            case GITHUB -> "https://github.com/login/oauth/authorize?client_id=${client-id}&redirect_uri=${redirect-uri}";
            case KAKAO -> "https://kauth.kakao.com/oauth/authorize?client_id=${client-id}&response_type=code&redirect_uri=${redirect-uri}";
        };
        return ResponseEntity.ok(loginUrl);
    }

    @Operation(summary = "현재 인증된 사용자 정보 조회",
            description = "JWT 토큰으로 인증된 현재 사용자의 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse userResponse = UserResponse.builder()
                .id(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .username(userPrincipal.getUsername())
                .provider(userPrincipal.getProvider())
                .profileImage(userPrincipal.getProfileImage())
                .build();
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "토큰 갱신",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "리프레시 토큰", required = true)
            @RequestHeader("Authorization") String refreshToken) {
        // Bearer 접두사 제거
        refreshToken = refreshToken.substring(7);

        // 토큰 검증 및 새로운 액세스 토큰 발급
        if (tokenProvider.validateToken(refreshToken)) {
            String userId = tokenProvider.getUserIdFromToken(refreshToken);
            String newAccessToken = tokenProvider.generateAccessToken(/* authentication object */);

            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getAccessTokenExpirationMs())
                    .build());
        }

        return ResponseEntity.status(401).build();
    }

    @Operation(summary = "로그아웃",
            description = "현재 사용자의 리프레시 토큰을 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        tokenProvider.invalidateToken(userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
}