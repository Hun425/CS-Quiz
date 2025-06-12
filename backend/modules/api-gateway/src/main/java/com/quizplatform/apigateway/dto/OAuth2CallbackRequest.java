package com.quizplatform.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(description = "OAuth2 콜백 요청")
@Builder
public record OAuth2CallbackRequest(
    @Schema(description = "OAuth2 제공자", example = "google")
    @NotBlank(message = "OAuth2 제공자는 필수입니다")
    String provider,
    
    @Schema(description = "Authorization Code")
    @NotBlank(message = "Authorization Code는 필수입니다")
    String code,
    
    @Schema(description = "State 값 (CSRF 방지)")
    String state
) {}