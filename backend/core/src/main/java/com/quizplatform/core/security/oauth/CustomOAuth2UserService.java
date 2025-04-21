package com.quizplatform.core.security.oauth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * OAuth2 사용자 정보 서비스 인터페이스
 * <p>
 * OAuth2 로그인 시 사용자 정보를 처리하는 서비스 인터페이스입니다.
 * 실제 구현체는 user 모듈에서 제공됩니다.
 * </p>
 */
public interface CustomOAuth2UserService extends OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /**
     * OAuth2 제공자로부터 사용자 정보를 로드합니다.
     *
     * @param userRequest OAuth2 사용자 정보 요청 객체
     * @return 처리된 OAuth2User 객체
     * @throws OAuth2AuthenticationException 인증 처리 중 오류 발생 시
     */
    @Override
    OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException;

    /**
     * 사용자 ID를 기반으로 사용자 정보를 로드합니다.
     *
     * @param id 조회할 사용자의 ID
     * @return Spring Security UserDetails 객체
     */
    UserDetails loadUserById(Long id);
}