package com.quizplatform.modules.user.application.event; // Or domain.event

import java.time.ZonedDateTime;

/**
 * 사용자가 등록되거나 정보가 업데이트되었음을 알리는 이벤트 DTO
 */
public record UserRegisteredOrUpdatedEvent(
        Long userId,
        String email,
        String username,
        String profileImageUrl,
        boolean isNewUser, // 새로 등록된 사용자인지 여부
        ZonedDateTime occurredAt
) {
    public UserRegisteredOrUpdatedEvent(Long userId, String email, String username, String profileImageUrl, boolean isNewUser) {
        this(userId, email, username, profileImageUrl, isNewUser, ZonedDateTime.now());
    }
} 