package com.quizplatform.common.adapter.config.security.oauth;

import lombok.Getter;

import java.util.Map;

/**
 * Kakao OAuth2 제공자로부터 받은 사용자 정보를 처리하는 클래스입니다.
 * {@link OAuth2UserInfo} 추상 클래스를 상속받아, Kakao 응답의 속성(attributes) 맵에서
 * 사용자 ID('id'), 이메일('kakao_account.email'), 닉네임('properties.nickname'),
 * 프로필 이미지 URL('properties.profile_image') 등을 추출하는 구체적인 로직을 구현합니다.
 * Kakao 응답은 중첩된 구조를 가지므로 해당 구조에 맞게 값을 추출합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * Kakao OAuth2 응답의 속성(attributes) 맵을 사용하여 KakaoOAuth2UserInfo 객체를 생성합니다.
     *
     * @param attributes Kakao API 응답에서 받은 사용자 정보 속성 맵
     */
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes); // 부모 클래스의 생성자 호출하여 속성 맵 저장
    }

    /**
     * Kakao 사용자 고유 ID를 반환합니다.
     * Kakao 응답에서는 숫자(Long 또는 Integer) 형태일 수 있으므로 문자열(String)로 변환하여 반환합니다.
     *
     * @return Kakao 사용자 ID (문자열)
     */
    @Override
    public String getId() {
        // 'id' 속성 값을 String으로 변환하여 반환
        return String.valueOf(attributes.get("id"));
    }

    /**
     * Kakao 사용자 이메일 주소를 반환합니다.
     * 이메일 정보는 'kakao_account' 객체 내부에 포함되어 있습니다.
     * 'kakao_account' 객체나 이메일 정보가 없을 경우 null을 반환합니다.
     *
     * @return Kakao 사용자 이메일 주소 (문자열) 또는 null
     */
    @Override
    public String getEmail() {
        // 'kakao_account' 속성 값을 Map으로 캐스팅
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        // account 맵이 null이면 이메일 정보 없음
        if (account == null) {
            return null;
        }
        // account 맵에서 'email' 속성 값을 String으로 캐스팅하여 반환
        return (String) account.get("email");
    }

    /**
     * Kakao 사용자 닉네임을 반환합니다.
     * 닉네임 정보는 'properties' 객체 내부에 포함되어 있습니다.
     * 'properties' 객체나 닉네임 정보가 없을 경우 null을 반환합니다.
     *
     * @return Kakao 사용자 닉네임 (문자열) 또는 null
     */
    @Override
    public String getName() {
        // 'properties' 속성 값을 Map으로 캐스팅
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        // properties 맵이 null이면 닉네임 정보 없음
        if (properties == null) {
            return null;
        }
        // properties 맵에서 'nickname' 속성 값을 String으로 캐스팅하여 반환
        return (String) properties.get("nickname");
    }

    /**
     * Kakao 사용자 프로필 이미지 URL을 반환합니다.
     * 프로필 이미지 URL 정보는 'properties' 객체 내부에 포함되어 있습니다.
     * 'properties' 객체나 프로필 이미지 URL 정보가 없을 경우 null을 반환합니다.
     *
     * @return Kakao 사용자 프로필 이미지 URL (문자열) 또는 null
     */
    @Override
    public String getImageUrl() {
        // 'properties' 속성 값을 Map으로 캐스팅
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        // properties 맵이 null이면 이미지 URL 정보 없음
        if (properties == null) {
            return null;
        }
        // properties 맵에서 'profile_image' 속성 값을 String으로 캐스팅하여 반환
        return (String) properties.get("profile_image");
    }
} 