package com.quizplatform.common.security.oauth;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.modules.user.domain.User;
import com.quizplatform.modules.user.repository.UserRepository;
import com.quizplatform.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 사용자 서비스 구현 클래스
 * 
 * <p>소셜 로그인(OAuth2) 인증을 처리하고, 해당 사용자 정보를 기반으로
 * 자체 사용자 데이터베이스와 연동하는 역할을 담당합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * OAuth2 사용자 정보 로드
     * 
     * @param userRequest OAuth2 사용자 요청 정보
     * @return 인증된 OAuth2 사용자
     * @throws OAuth2AuthenticationException OAuth2 인증 예외
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("OAuth2 사용자 처리 오류", ex);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "OAuth2 인증 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * OAuth2 사용자 정보 처리
     * 
     * @param userRequest OAuth2 사용자 요청 정보
     * @param oAuth2User OAuth2 사용자 정보
     * @return 인증된 사용자 주체
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이메일이 제공되지 않았습니다.");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 사용자 정보 업데이트
            updateExistingUser(user, oAuth2UserInfo);
        } else {
            // 새 사용자 등록
            user = registerNewUser(registrationId, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    /**
     * 제공자별 OAuth2 사용자 정보 객체 생성
     * 
     * @param registrationId OAuth2 제공자 ID
     * @param attributes OAuth2 사용자 속성
     * @return OAuth2 사용자 정보
     */
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GithubOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    registrationId + " 로그인은 지원하지 않습니다.");
        };
    }

    /**
     * 새 사용자 등록
     * 
     * @param registrationId OAuth2 제공자 ID
     * @param oAuth2UserInfo OAuth2 사용자 정보
     * @return 등록된 사용자 엔티티
     */
    private User registerNewUser(String registrationId, OAuth2UserInfo oAuth2UserInfo) {
        return userService.registerOAuth2User(registrationId, oAuth2UserInfo);
    }

    /**
     * 기존 사용자 정보 업데이트
     * 
     * @param user 사용자 엔티티
     * @param oAuth2UserInfo OAuth2 사용자 정보
     */
    private void updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        userService.updateOAuth2User(user, oAuth2UserInfo);
    }
}