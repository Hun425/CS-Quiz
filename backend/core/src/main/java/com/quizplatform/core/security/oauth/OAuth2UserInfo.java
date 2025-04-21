package com.quizplatform.core.security.oauth;

import java.util.Map;

/**
 * OAuth2 사용자 정보 추상 클래스
 * 
 * <p>다양한 OAuth2 제공자(Google, Github, Kakao 등)로부터 획득한
 * 사용자 정보를 표준화된 방식으로 접근할 수 있도록 하는 추상 클래스입니다.</p>
 * 
 * @author 채기훈
 * @since JDK 17
 */
public abstract class OAuth2UserInfo {
    
    /**
     * OAuth2 제공자로부터 받은 사용자 속성
     */
    protected Map<String, Object> attributes;

    /**
     * 생성자
     * 
     * @param attributes OAuth2 사용자 속성
     */
    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * 사용자 ID 조회
     * 
     * @return OAuth2 제공자에서의 사용자 ID
     */
    public abstract String getId();

    /**
     * 사용자 이름 조회
     * 
     * @return 사용자 이름
     */
    public abstract String getName();

    /**
     * 사용자 이메일 조회
     * 
     * @return 사용자 이메일
     */
    public abstract String getEmail();

    /**
     * 사용자 프로필 이미지 URL 조회
     * 
     * @return 프로필 이미지 URL
     */
    public abstract String getImageUrl();
}