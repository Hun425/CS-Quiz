package com.quizplatform.modules.user.application.dto;

// 사용자 프로필 수정을 위한 DTO
public record UserUpdateRequest(
        String username,
        String profileImage
) {}