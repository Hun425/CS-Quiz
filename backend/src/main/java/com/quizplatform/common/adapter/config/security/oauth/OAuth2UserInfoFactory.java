package com.quizplatform.common.adapter.config.security.oauth;

// TODO: AuthProvider enum의 실제 위치 확인 후 import 경로 재검토 필요 (현재 user 모듈로 가정)
import com.quizplatform.user.domain.AuthProvider;

import java.util.Map;

/**
 * OAuth2 제공자(Provider) 타입에 따라 적절한 {@link OAuth2UserInfo} 구현 클래스의 인스턴스를
 * 생성하여 반환하는 팩토리 클래스입니다.
 * 각 제공자별 응답 구조에 맞는 파싱 로직을 가진 클래스를 선택하는 역할을 합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public class OAuth2UserInfoFactory {

    /**
     * 주어진 OAuth2 제공자(AuthProvider) 타입과 사용자 속성(attributes) 맵을 기반으로
     * 해당 제공자에 맞는 OAuth2UserInfo 구현 객체를 생성하여 반환합니다.
     *
     * @param authProvider OAuth2 제공자 타입 (GOOGLE, KAKAO, GITHUB 등)
     * @param attributes   OAuth2 제공자로부터 받은 사용자의 원본 속성 맵
     * @return 제공자 타입에 맞는 OAuth2UserInfo 구현 객체 (예: GoogleOAuth2UserInfo)
     * @throws IllegalArgumentException 지원하지 않는 제공자 타입(authProvider)이 입력된 경우 발생
     */
    public static OAuth2UserInfo getOAuth2UserInfo(AuthProvider authProvider, Map<String, Object> attributes) {
        // authProvider 값에 따라 적절한 OAuth2UserInfo 구현 클래스를 선택하여 인스턴스화
        // GoogleOAuth2UserInfo, KakaoOAuth2UserInfo, GithubOAuth2UserInfo 는
        // 동일 패키지(com.quizplatform.common.adapter.config.security.oauth) 내에 위치해야 함
        return switch (authProvider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes); // Google 제공자인 경우
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);   // Kakao 제공자인 경우
            case GITHUB -> new GithubOAuth2UserInfo(attributes); // GitHub 제공자인 경우
            // 다른 제공자 지원 추가 시 여기에 case 추가
            default ->
                // 지원하지 않는 제공자 타입인 경우 예외 발생
                    throw new IllegalArgumentException("Invalid or unsupported OAuth2 Provider Type: " + authProvider);
        };
    }
} 