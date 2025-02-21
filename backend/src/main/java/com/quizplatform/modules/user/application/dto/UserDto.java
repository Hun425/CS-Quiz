package com.quizplatform.modules.user.application.dto;

import com.quizplatform.core.domain.user.AuthProvider;

import com.quizplatform.core.domain.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.UUID;

public class UserDto {

    @Schema(description = "사용자 응답 DTO")
    public record Response(
            @Schema(description = "사용자 ID") Long id,
            @Schema(description = "이메일") String email,
            @Schema(description = "사용자명") String username,
            @Schema(description = "프로필 이미지 URL") String profileImage,
            @Schema(description = "인증 제공자") AuthProvider provider,
            @Schema(description = "역할") UserRole role,
            @Schema(description = "계정 활성화 여부") boolean isActive,
            @Schema(description = "마지막 로그인 시간") ZonedDateTime lastLoginAt
    ) {}

    @Schema(description = "프로필 수정 요청 DTO")
    public record UpdateRequest(
            @Schema(description = "변경할 사용자명") String username,
            @Schema(description = "변경할 프로필 이미지 URL") String profileImage
    ) {}

    @Schema(description = "회원 상태 변경 요청 DTO")
    public record StatusUpdateRequest(
            @Schema(description = "활성화 여부") boolean isActive
    ) {}
}