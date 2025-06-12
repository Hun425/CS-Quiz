package com.quizplatform.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * OAuth2 사용자 요청 DTO (API Gateway용)
 * 
 * <p>OAuth2 제공자로부터 받은 사용자 정보를 User Service로 전달하기 위한 DTO입니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Schema(description = "OAuth2 사용자 정보 요청")
@Builder
public record OAuth2UserRequest(
    
    @Schema(description = "OAuth2 제공자", example = "google")
    @NotBlank(message = "OAuth2 제공자는 필수입니다")
    String provider,
    
    @Schema(description = "OAuth2 제공자의 사용자 ID", example = "123456789")
    @NotBlank(message = "OAuth2 사용자 ID는 필수입니다")
    String providerId,
    
    @Schema(description = "사용자 이메일", example = "user@example.com")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    @NotBlank(message = "이메일은 필수입니다")
    String email,
    
    @Schema(description = "사용자 표시 이름", example = "홍길동")
    @NotBlank(message = "표시 이름은 필수입니다")
    String displayName,
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl
) {
}