package com.quizplatform.core.service.user;

import com.quizplatform.core.dto.AuthResponse;

/**
 * 인증 관련 서비스 인터페이스
 *
 * <p>OAuth2 소셜 로그인 인증, JWT 토큰 발급, 리프레시 및 로그아웃 기능을 정의합니다.
 * 다양한 소셜 로그인 제공자(Google, GitHub, Kakao)에 대한 처리를 지원합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface AuthService {

    /**
     * 소셜 로그인 제공자의 인증 URL을 생성합니다.
     *
     * <p>이 URL로 사용자를 리다이렉트하면 소셜 로그인 프로세스가 시작됩니다.</p>
     *
     * @param provider 소셜 로그인 제공자 (google, github, kakao 등)
     * @return 인증 URL 문자열
     */
    String getAuthorizationUrl(String provider);

    /**
     * OAuth2 인증 코드를 처리하고 사용자 정보와 토큰을 포함한 인증 응답을 생성합니다.
     *
     * <p>사용자가 존재하지 않으면 새로 생성하고, 이미 존재하면 정보를 업데이트합니다.</p>
     *
     * @param provider 소셜 로그인 제공자
     * @param code 인증 코드
     * @return 액세스 토큰, 리프레시 토큰, 사용자 정보를 포함한 인증 응답
     */
    AuthResponse processOAuth2Login(String provider, String code);

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     *
     * <p>사용자 정보도 함께 반환하여 클라이언트가 필요한 정보를 모두 가질 수 있도록 합니다.</p>
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰과 사용자 정보를 포함한 인증 응답
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * 토큰을 무효화하고 사용자를 로그아웃 처리합니다.
     *
     * @param userId 로그아웃할 사용자 ID
     */
    void logout(String userId);

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 추출된 사용자 ID
     */
    String getUserIdFromToken(String token);

    /**
     * 승인된 리다이렉트 URI를 반환합니다.
     *
     * @return 승인된 리다이렉트 URI
     */
    String getAuthorizedRedirectUri();
}