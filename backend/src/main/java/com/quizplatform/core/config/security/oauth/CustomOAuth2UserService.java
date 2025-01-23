package com.quizplatform.core.config.security.oauth;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.config.security.oauth.OAuth2UserInfo;
import com.quizplatform.core.config.security.oauth.OAuth2UserInfoFactory;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.exception.OAuth2AuthenticationProcessingException;
import com.quizplatform.core.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException("Authentication failed: " + ex.getMessage());
        }
    }

    @Transactional
    protected OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // OAuth2 제공자 확인
        AuthProvider authProvider = AuthProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                authProvider,
                oauth2User.getAttributes()
        );

        // 이메일 검증
        validateEmail(oauth2UserInfo.getEmail());

        // 사용자 처리
        User user = processUser(oauth2UserInfo, authProvider);

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        userRepository.save(user);

        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
    }

    @Transactional
    protected User processUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        Optional<User> userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail());

        if (userOptional.isPresent()) {
            return updateExistingUser(userOptional.get(), oauth2UserInfo, authProvider);
        }

        return registerNewUser(oauth2UserInfo, authProvider);
    }

    private User updateExistingUser(User user, OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 제공자 검증
        if (!user.getProvider().equals(authProvider)) {
            throw new OAuth2AuthenticationProcessingException(
                    String.format("이미 %s 계정으로 가입된 이메일입니다. 해당 계정으로 로그인해주세요.", user.getProvider())
            );
        }

        // 프로필 업데이트
        user.updateProfile(
                oauth2UserInfo.getName(),
                oauth2UserInfo.getImageUrl()
        );

        return userRepository.save(user);
    }

    private User registerNewUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 사용자명 중복 체크 및 생성
        String username = generateUniqueUsername(oauth2UserInfo.getName());

        User user = User.builder()
                .provider(authProvider)
                .providerId(oauth2UserInfo.getId())
                .email(oauth2UserInfo.getEmail())
                .username(username)
                .profileImage(oauth2UserInfo.getImageUrl())
                .build();

        return userRepository.save(user);
    }

    private String generateUniqueUsername(String baseName) {
        String username = baseName;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = String.format("%s%d", baseName, suffix++);
        }

        return username;
    }

    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}