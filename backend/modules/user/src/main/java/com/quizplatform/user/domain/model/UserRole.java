package com.quizplatform.user.domain.model;

/**
 * 사용자 역할 열거형
 * 
 * <p>시스템 내 사용자의 권한 레벨을 정의합니다.
 * 권한에 따라 접근 가능한 기능과 리소스가 달라집니다.</p>
 */
public enum UserRole {
    /**
     * 일반 사용자
     * 기본적인 퀴즈 풀이 및 배틀 참여 권한
     */
    USER,
    
    /**
     * 프리미엄 사용자
     * 추가 기능 및 콘텐츠 접근 권한
     */
    PREMIUM,
    
    /**
     * 관리자
     * 시스템 관리 및 모든 기능에 대한 접근 권한
     */
    ADMIN,
    
    /**
     * 퀴즈 제작자
     * 퀴즈 생성 및 관리에 대한 확장된 권한
     */
    CREATOR
} 