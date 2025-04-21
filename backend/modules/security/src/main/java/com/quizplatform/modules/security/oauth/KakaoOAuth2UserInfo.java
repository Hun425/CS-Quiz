package com.quizplatform.modules.security.oauth;

import java.util.Map;

/**
 * Kakao OAuth2 사용자 정보 구현 클래스
 * <p>
 * Kakao OAuth2 제공자로부터 획득한 사용자 정보를 처리하는 클래스입니다.
 * </p>
 */
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * 생성자
     * 
     * @param attributes Kakao OAuth2 사용자 속성
     */
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("nickname");
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("profile_image");
    }
}