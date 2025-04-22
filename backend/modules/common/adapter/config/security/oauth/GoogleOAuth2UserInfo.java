package com.quizplatform.common.adapter.config.security.oauth;

import lombok.Getter;

import java.util.Map;

/**
 * Google OAuth2 제공자로부터 받은 사용자 정보를 처리하는 클래스입니다.
 * {@link OAuth2UserInfo} 추상 클래스를 상속받아, Google 응답의 속성(attributes) 맵에서
 * 사용자 ID('sub'), 이메일('email'), 이름('name'), 프로필 사진 URL('picture') 등을
 * 추출하는 구체적인 로직을 구현합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * Google OAuth2 응답의 속성(attributes) 맵을 사용하여 GoogleOAuth2UserInfo 객체를 생성합니다.
     *
     * @param attributes Google API 응답에서 받은 사용자 정보 속성 맵
     */
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes); // 부모 클래스의 생성자 호출하여 속성 맵 저장
    }

    /**
     * Google 사용자의 고유 식별자(Subject Identifier)를 반환합니다.
     * Google 응답에서는 'sub' 필드에 해당합니다.
     *
     * @return Google 사용자 ID (문자열)
     */
    @Override
    public String getId() {
        // 'sub' 속성 값을 String으로 캐스팅하여 반환
        return (String) attributes.get("sub");
    }

    /**
     * Google 사용자 이메일 주소를 반환합니다.
     *
     * @return Google 사용자 이메일 주소 (문자열)
     */
    @Override
    public String getEmail() {
        // 'email' 속성 값을 String으로 캐스팅하여 반환
        return (String) attributes.get("email");
    }

    /**
     * Google 사용자 전체 이름(Full Name)을 반환합니다.
     *
     * @return Google 사용자 이름 (문자열)
     */
    @Override
    public String getName() {
        // 'name' 속성 값을 String으로 캐스팅하여 반환
        return (String) attributes.get("name");
    }

    /**
     * Google 사용자 프로필 사진 URL을 반환합니다.
     *
     * @return Google 사용자 프로필 사진 URL (문자열)
     */
    @Override
    public String getImageUrl() {
        // 'picture' 속성 값을 String으로 캐스팅하여 반환
        return (String) attributes.get("picture");
    }
} 