package com.quizplatform.core.config.security.oauth;

import com.quizplatform.core.config.security.oauth.OAuth2UserInfo;
import com.quizplatform.core.config.security.oauth.OAuth2UserInfoFactory;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            // OAuth2 인증 처리 중 예외 발생 시 처리
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // OAuth2 제공자(Google, Github, Kakao) 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        AuthProvider authProvider = AuthProvider.valueOf(registrationId);

        // OAuth2UserInfo 객체 생성
        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authProvider, oauth2User.getAttributes());

        // 이메일 정보가 없는 경우 예외 처리
        String email = oauth2UserInfo.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // 기존 사용자 조회 또는 새로운 사용자 생성
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 다른 OAuth2 제공자로 가입한 경우 예외 처리
            if (!user.getProvider().equals(authProvider)) {
                throw new OAuth2AuthenticationException(
                        "You're signed up with " + user.getProvider() + ". Please use that to login.");
            }
            // 기존 사용자 정보 업데이트
            user = updateExistingUser(user, oauth2UserInfo);
        } else {
            // 새로운 사용자 등록
            user = registerNewUser(oauth2UserInfo, authProvider);
        }

        return com.quizplatform.core.security.UserPrincipal.create(user, oauth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        User user = User.builder()
                .provider(authProvider)
                .providerId(oauth2UserInfo.getId())
                .email(oauth2UserInfo.getEmail())
                .username(oauth2UserInfo.getName())
                .profileImage(oauth2UserInfo.getImageUrl())
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo oauth2UserInfo) {
        user.updateProfile(
                oauth2UserInfo.getName(),
                oauth2UserInfo.getImageUrl()
        );
        return userRepository.save(user);
    }
}