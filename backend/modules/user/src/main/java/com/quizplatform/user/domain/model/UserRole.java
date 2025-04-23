package com.quizplatform.user.domain.model;

/**
 * 사용자 권한 열거형
 * 
 * <p>시스템에서 사용되는 사용자 권한 수준을 정의합니다.</p>
 */
public enum UserRole {
    /**
     * 일반 사용자
     * 기본적인 기능만 사용 가능
     */
    USER,
    
    /**
     * 퀴즈 관리자
     * 퀴즈 생성 및 관리 권한 있음
     */
    QUIZ_ADMIN,
    
    /**
     * 배틀 관리자
     * 배틀 관리 및 토너먼트 주최 권한 있음
     */
    BATTLE_ADMIN,
    
    /**
     * 시스템 관리자
     * 모든 기능에 대한 접근 권한 있음
     */
    ADMIN,
    
    /**
     * 시스템 소유자
     * 시스템 전체 관리 권한 있음
     */
    OWNER
} 