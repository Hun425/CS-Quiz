package com.quizplatform.user.domain.model;

/**
 * 인증 제공자 열거형
 * 
 * <p>지원하는 OAuth2 인증 제공자를 정의합니다.</p>
 */
public enum AuthProvider {
    /**
     * 구글 로그인
     */
    GOOGLE,
    
    /**
     * 깃허브 로그인
     */
    GITHUB,
    
    /**
     * 카카오 로그인
     */
    KAKAO,
    
    /**
     * 네이버 로그인
     */
    NAVER,
    
    /**
     * 로컬 로그인 (이메일/비밀번호)
     */
    LOCAL
} 