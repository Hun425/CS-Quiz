package com.quizplatform.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "로그인 응답")
@Builder
public record LoginResponse(
    @Schema(description = "액세스 토큰")
    String accessToken,
    
    @Schema(description = "리프레시 토큰")
    String refreshToken,
    
    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType,
    
    @Schema(description = "토큰 만료 시간 (초)", example = "3600")
    long expiresIn,
    
    @Schema(description = "사용자 정보")
    UserInfo user
) {
    @Schema(description = "사용자 정보")
    @Builder
    public record UserInfo(
        @Schema(description = "사용자 ID")
        Long id,
        
        @Schema(description = "이메일")
        String email,
        
        @Schema(description = "표시 이름")
        String displayName,
        
        @Schema(description = "권한 목록")
        List<String> roles
    ) {}
}