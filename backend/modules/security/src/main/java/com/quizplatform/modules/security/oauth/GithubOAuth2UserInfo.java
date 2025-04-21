package com.quizplatform.modules.security.oauth;

import java.util.Map;

/**
 * GitHub OAuth2 사용자 정보 구현 클래스
 * <p>
 * GitHub OAuth2 제공자로부터 획득한 사용자 정보를 처리하는 클래스입니다.
 * </p>
 */
public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * 생성자
     * 
     * @param attributes GitHub OAuth2 사용자 속성
     */
    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}