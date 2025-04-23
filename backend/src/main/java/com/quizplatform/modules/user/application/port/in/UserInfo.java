package com.quizplatform.modules.user.application.port.in; // Or a dto package

/**
 * 사용자 기본 정보 조회 결과를 담는 DTO
 */
public record UserInfo(
        Long userId,
        String username,
        String profileImageUrl,
        int level // 필요시 레벨 정보 포함
) {
} 