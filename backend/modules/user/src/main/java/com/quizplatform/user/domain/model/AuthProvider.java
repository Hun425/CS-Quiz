package com.quizplatform.user.domain.model;

/**
 * 인증 제공자 열거형
 * 
 * <p>사용자 인증에 사용되는 외부 인증 제공자를 정의합니다.
 * OAuth2 인증 프로세스에서 사용됩니다.</p>
 */
public enum AuthProvider {
    /**
     * 일반 이메일/비밀번호 로그인
     */
    LOCAL,
    
    /**
     * 구글 OAuth2 인증
     */
    GOOGLE,
    
    /**
     * 카카오 OAuth2 인증
     */
    KAKAO,
    
    /**
     * 깃허브 OAuth2 인증
     */
    GITHUB,
    
    /**
     * 네이버 OAuth2 인증
     */
    NAVER
} 