package com.quizplatform.user.application.port.in.dto;

import com.quizplatform.user.domain.model.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * 인증 성공 결과 DTO
 */
@Getter
public class AuthenticationResult {

    private final Long userId;
    private final String username;
    private final Set<UserRole> roles;
    private final String accessToken;
    private final String refreshToken; // 선택 사항

    @Builder
    public AuthenticationResult(Long userId, String username, Set<UserRole> roles, String accessToken, String refreshToken) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
} 