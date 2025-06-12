package com.quizplatform.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "토큰 갱신 응답")
@Builder
public record RefreshTokenResponse(
    @Schema(description = "새로운 액세스 토큰")
    String accessToken,
    
    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType,
    
    @Schema(description = "토큰 만료 시간 (초)", example = "3600")
    long expiresIn
) {}