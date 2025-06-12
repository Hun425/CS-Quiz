package com.quizplatform.user.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(description = "OAuth2 사용자 정보 요청")
@Builder
public record OAuth2UserRequest(
    @Schema(description = "OAuth2 제공자", example = "GOOGLE")
    @NotBlank(message = "OAuth2 제공자는 필수입니다")
    String provider,
    
    @Schema(description = "제공자에서의 사용자 ID")
    @NotBlank(message = "제공자 사용자 ID는 필수입니다")
    String providerId,
    
    @Schema(description = "이메일", example = "user@example.com")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,
    
    @Schema(description = "사용자명", example = "홍길동")
    String name,
    
    @Schema(description = "프로필 이미지 URL")
    String profileImage
) {}