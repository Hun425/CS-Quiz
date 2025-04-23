package com.quizplatform.user.infrastructure.http.dto;

import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * 사용자 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String profileImage;
    private UserRole role;
    private boolean active;
    private int totalPoints;
    private int level;
    private int experience;
    private int requiredExperience;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    
    /**
     * 사용자 엔티티를 DTO로 변환
     * 
     * @param user 사용자 엔티티
     * @return 사용자 응답 DTO
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .active(user.isActive())
                .totalPoints(user.getTotalPoints())
                .level(user.getLevel())
                .experience(user.getExperience())
                .requiredExperience(user.getRequiredExperience())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 