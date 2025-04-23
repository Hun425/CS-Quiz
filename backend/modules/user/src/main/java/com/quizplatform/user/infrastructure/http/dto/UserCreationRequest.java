package com.quizplatform.user.infrastructure.http.dto;

import com.quizplatform.user.domain.model.AuthProvider;
import com.quizplatform.user.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 생성 요청 DTO
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {
    
    @NotBlank(message = "인증 제공자는 필수입니다")
    private String provider;
    
    @NotBlank(message = "제공자 ID는 필수입니다")
    private String providerId;
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;
    
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3자 이상 50자 이하여야 합니다")
    private String username;
    
    private String profileImage;
    
    /**
     * DTO를 엔티티로 변환
     * 
     * @return 사용자 엔티티
     */
    public User toEntity() {
        return User.builder()
                .provider(AuthProvider.valueOf(provider.toUpperCase()))
                .providerId(providerId)
                .email(email)
                .username(username)
                .profileImage(profileImage)
                .build();
    }
} 