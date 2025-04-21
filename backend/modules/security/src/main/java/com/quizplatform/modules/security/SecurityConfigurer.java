package com.quizplatform.modules.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 보안 구성을 위한 인터페이스
 * <p>
 * 각 모듈이 이 인터페이스를 구현하여 보안 설정을 제공할 수 있습니다.
 * API 게이트웨이는 이 인터페이스를 통해 각 모듈의 보안 설정을 통합합니다.
 * </p>
 */
public interface SecurityConfigurer {

    /**
     * 보안 필터 체인을 설정합니다.
     *
     * @param http HttpSecurity 객체
     * @return 설정된 SecurityFilterChain
     * @throws Exception 보안 구성 중 발생할 수 있는 예외
     */
    SecurityFilterChain configureFilterChain(HttpSecurity http) throws Exception;
    
    /**
     * 인증이 필요 없는 경로(화이트 리스트) 목록을 반환합니다.
     *
     * @return 화이트 리스트 경로 배열
     */
    String[] getWhiteList();
}