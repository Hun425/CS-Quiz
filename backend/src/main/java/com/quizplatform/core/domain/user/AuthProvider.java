package com.quizplatform.core.domain.user;

import lombok.Getter;

/**
 * 인증 제공자 열거형 클래스
 * 
 * <p>시스템에서 지원하는 OAuth 인증 제공자를 정의합니다.
 * 각 제공자는 사용자 인증을 위한 서로 다른 외부 서비스를 나타냅니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public enum AuthProvider {
    /**
     * Google 인증 제공자
     */
    GOOGLE("GOOGLE"),
    
    /**
     * GitHub 인증 제공자
     */
    GITHUB("GITHUB"),
    
    /**
     * Kakao 인증 제공자
     */
    KAKAO("KAKAO"),
    
    /**
     * 테스트용 인증 제공자
     */
    TEST("TEST");

    /**
     * 제공자 이름
     */
    private final String providerName;

    /**
     * 인증 제공자 생성자
     * 
     * @param providerName 제공자 이름
     */
    AuthProvider(String providerName) {
        this.providerName = providerName;
    }
}