package com.quizplatform.core.service.test;

import com.quizplatform.core.dto.user.TestTokenResponse;

/**
 * 테스트 인증 서비스 인터페이스
 * 
 * <p>개발 및 테스트 환경에서 JWT 토큰 발급 및 관리를 위한 서비스 인터페이스입니다.
 * 테스트용 사용자를 생성하고 임시 인증 토큰을 발급하는 기능을 제공합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface TestAuthService {

    /**
     * 테스트용 JWT 토큰을 생성합니다.
     * 
     * <p>사용자 이름으로 사용자를 검색하고, 존재하지 않으면 임시 사용자를 생성하여 토큰을 발급합니다.
     * 발급된 토큰은 실제 인증 시스템과 동일하게 작동합니다.</p>
     * 
     * @param username 테스트 사용자 이름
     * @return 테스트 토큰 응답 객체 (액세스 토큰, 리프레시 토큰 포함)
     * @throws com.quizplatform.core.exception.BusinessException 사용자명이 이미 존재하는 경우 발생
     */
    TestTokenResponse generateTestToken(String username);
} 