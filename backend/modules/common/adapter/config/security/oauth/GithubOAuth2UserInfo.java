package com.quizplatform.common.adapter.config.security.oauth;

import lombok.Getter;

import java.util.Map;

/**
 * GitHub OAuth2 제공자로부터 받은 사용자 정보를 처리하는 클래스입니다.
 * {@link OAuth2UserInfo} 추상 클래스를 상속받아, GitHub 응답의 속성(attributes) 맵에서
 * 사용자 ID, 이메일, 이름(로그인 ID), 이미지 URL 등을 추출하는 구체적인 로직을 구현합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
class GithubOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * GitHub OAuth2 응답의 속성(attributes) 맵을 사용하여 GithubOAuth2UserInfo 객체를 생성합니다.
     *
     * @param attributes GitHub API 응답에서 받은 사용자 정보 속성 맵
     */
    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes); // 부모 클래스의 생성자 호출하여 속성 맵 저장
    }

    /**
     * GitHub 사용자 고유 ID를 반환합니다.
     * GitHub 응답에서는 정수(Integer) 형태이므로 문자열(String)로 변환하여 반환합니다.
     *
     * @return GitHub 사용자 ID (문자열)
     */
    @Override
    public String getId() {
        // 'id' 속성 값을 Integer로 캐스팅 후 String으로 변환
        return ((Integer) attributes.get("id")).toString();
    }

    /**
     * GitHub 사용자 이메일 주소를 반환합니다.
     * 사용자가 GitHub 프로필에 이메일을 공개 설정하지 않은 경우 null일 수 있습니다.
     *
     * @return GitHub 사용자 이메일 주소 (문자열) 또는 null
     */
    @Override
    public String getEmail() {
        // 'email' 속성 값을 String으로 캐스팅하여 반환
        return (String) attributes.get("email");
    }

    /**
     * GitHub 사용자 이름(로그인 ID)을 반환합니다.
     * 일반적으로 GitHub 계정의 고유한 로그인 ID입니다.
     *
     * @return GitHub 사용자 이름 (로그인 ID, 문자열)
     */
    @Override
    public String getName() {
        // 'login' 속성 값을 String으로 캐스팅하여 반환
        return (String) attributes.get("login");
    }

    /**
     * GitHub 사용자 프로필 이미지(아바타) URL을 반환합니다.
     *
     * @return GitHub 사용자 아바타 URL (문자열)
     */
    @Override
    public String getImageUrl() {
        // 'avatar_url' 속성 값을 String으로 캐스팅하여 반환
        return (String) attributes.get("avatar_url");
    }
} 