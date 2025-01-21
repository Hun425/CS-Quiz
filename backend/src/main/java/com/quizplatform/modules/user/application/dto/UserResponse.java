package com.quizplatform.modules.user.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        String profileImage,
        String role,
        Integer totalPoints,
        LocalDateTime lastLogin,
        LocalDateTime createdAt
) {}