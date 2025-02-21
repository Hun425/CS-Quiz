package com.quizplatform.core.dto;

import com.quizplatform.core.domain.user.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String profileImage;
    private String role;
    private Integer totalPoints;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private AuthProvider provider;  // 이 필드를 추가
}