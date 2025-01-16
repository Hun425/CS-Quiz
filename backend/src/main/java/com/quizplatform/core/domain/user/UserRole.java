package com.quizplatform.core.domain.user;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN"),
    MODERATOR("MODERATOR");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }
}