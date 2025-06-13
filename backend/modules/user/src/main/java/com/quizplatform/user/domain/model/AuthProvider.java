package com.quizplatform.user.domain.model;

/**
 * 인증 제공자 열거형
 * 
 * <p>지원하는 OAuth2 인증 제공자를 정의합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
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
    NAVER
    
    // OAuth2 전용 로그인으로 전환하여 LOCAL 제거
} 