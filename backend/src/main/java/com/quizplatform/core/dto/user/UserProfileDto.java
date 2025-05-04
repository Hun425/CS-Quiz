package com.quizplatform.core.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 프로필 DTO
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String profileImage;
    private Integer level;
    private Integer experience;
    private Integer requiredExperience;
    private Integer totalPoints;
    private String joinedAt;
    private String lastLogin;


}


