package com.quizplatform.modules.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 프로필 업데이트 요청 DTO
@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String username;
    private String profileImage;
}
