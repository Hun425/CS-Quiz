package com.quizplatform.modules.user.dto;

import com.quizplatform.modules.user.domain.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 DTO
 * <p>
 * 사용자 프로필 정보를 클라이언트에 반환하기 위한 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String profileImage;
    private AuthProvider provider;
    private LocalDateTime createdAt;
    private String bio;
}