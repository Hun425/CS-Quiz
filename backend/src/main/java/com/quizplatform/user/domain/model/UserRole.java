package com.quizplatform.user.domain.model;

import lombok.Getter;

/**
 * 사용자 권한 열거형 클래스
 *
 * <p>시스템 내 사용자의 권한 레벨을 정의합니다.
 * 각 권한은 서로 다른 기능 접근 수준을 가집니다.</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public enum UserRole {
    /**
     * 일반 사용자 권한
     */
    ROLE_USER,

    /**
     * 관리자 권한
     */
    ROLE_ADMIN,

    /**
     * 중재자 권한
     */
    MODERATOR("MODERATOR");

    /**
     * 권한 이름
     */
    private final String roleName;

    /**
     * 권한 생성자
     *
     * @param roleName 권한 이름
     */
    UserRole(String roleName) {
        this.roleName = roleName;
    }

    UserRole() {
        this.roleName = name();
    }
} 