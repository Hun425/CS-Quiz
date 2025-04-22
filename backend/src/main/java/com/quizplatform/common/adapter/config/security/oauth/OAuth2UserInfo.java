package com.quizplatform.common.adapter.config.security.oauth;

import lombok.Getter;
import java.util.Map;

/**
 * 다양한 OAuth2 제공자(Provider)로부터 받은 사용자 정보(attributes)를
 * 공통된 방식으로 접근하기 위한 추상 클래스입니다.
 * 각 제공자별 구현 클래스(예: GoogleOAuth2UserInfo, KakaoOAuth2UserInfo)는
 * 이 클래스를 상속받아, 제공자별 응답 구조에 맞게 사용자 ID, 이메일, 이름, 이미지 URL 등을
 * 추출하는 구체적인 로직을 구현해야 합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
public abstract class OAuth2UserInfo {
    /** OAuth2 제공자로부터 받은 사용자의 원본 속성(attributes) 맵 */
    protected Map<String, Object> attributes;

    /**
     * OAuth2 제공자로부터 받은 속성 맵을 사용하여 OAuth2UserInfo 객체를 초기화합니다.
     *
     * @param attributes OAuth2 제공자의 사용자 정보 응답에서 추출된 속성 맵
     */
    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * 사용자의 고유 식별자(ID)를 반환합니다.
     * 각 OAuth2 제공자별 구현 클래스에서 해당 제공자의 고유 ID 필드를 반환하도록 구현해야 합니다.
     *
     * @return 사용자의 고유 ID (문자열)
     */
    public abstract String getId();

    /**
     * 사용자의 이메일 주소를 반환합니다.
     * 각 OAuth2 제공자별 구현 클래스에서 해당 제공자의 이메일 필드를 반환하도록 구현해야 합니다.
     * 이메일 정보가 제공되지 않는 경우 null을 반환할 수 있습니다.
     *
     * @return 사용자의 이메일 주소 (문자열) 또는 null
     */
    public abstract String getEmail();

    /**
     * 사용자의 이름을 반환합니다.
     * 제공자에 따라 전체 이름(full name), 닉네임(nickname), 로그인 ID 등이 될 수 있습니다.
     * 각 OAuth2 제공자별 구현 클래스에서 적절한 이름 필드를 반환하도록 구현해야 합니다.
     *
     * @return 사용자의 이름 (문자열)
     */
    public abstract String getName();

    /**
     * 사용자의 프로필 이미지 URL을 반환합니다.
     * 각 OAuth2 제공자별 구현 클래스에서 해당 제공자의 프로필 이미지 URL 필드를 반환하도록 구현해야 합니다.
     * 이미지 URL 정보가 제공되지 않는 경우 null을 반환할 수 있습니다.
     *
     * @return 사용자의 프로필 이미지 URL (문자열) 또는 null
     */
    public abstract String getImageUrl();
} 