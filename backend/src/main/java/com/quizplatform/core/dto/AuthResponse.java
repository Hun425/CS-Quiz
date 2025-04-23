package com.quizplatform.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "인증 응답")
public class AuthResponse {
    @Schema(description = "액세스 토큰")
    private final String accessToken;

    @Schema(description = "리프레시 토큰")
    private final String refreshToken;

    @Schema(description = "이메일")
    private final String email;

    @Schema(description = "유저 이름")
    private final String username;


    @Schema(description = "토큰 타입", example = "Bearer")
    private final String tokenType;

    @Schema(description = "액세스 토큰 만료 시간 (밀리초)")
    private final long expiresIn;
}
