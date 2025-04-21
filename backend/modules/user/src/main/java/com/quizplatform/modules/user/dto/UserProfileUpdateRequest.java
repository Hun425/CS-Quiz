package com.quizplatform.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 업데이트 요청 DTO
 * <p>
 * 사용자 프로필 정보 업데이트 요청을 위한 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 20, message = "사용자명은 3자 이상 20자 이하여야 합니다")
    private String username;
    
    @Size(max = 255, message = "프로필 이미지 URL은 255자 이하여야 합니다")
    private String profileImage;
    
    @Size(max = 500, message = "소개는 500자 이하여야 합니다")
    private String bio;
}