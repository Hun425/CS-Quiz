package com.quizplatform.core.config.security.oauth;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final com.quizplatform.core.repository.user.UserLevelRepository userLevelRepository;
    private final Random random = new Random();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("OAuth2 인증 처리 중 오류 발생", ex);
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
        // 여기서 제공자 검증 코드를 제거
        // 대신 현재 사용자가 다른 제공자로 등록되어 있다면 현재 제공자 정보로 업데이트

        if (!user.getProvider().equals(authProvider)) {
            log.info("사용자 인증 제공자 업데이트: {} -> {}, 사용자: {}",
                    user.getProvider(), authProvider, user.getEmail());

            // 기존 제공자에서 새 제공자로 업데이트
            user.updateProvider(authProvider, oauth2UserInfo.getId());
        }

        // 프로필 이미지 업데이트 (사용자명은 유지)
        if (StringUtils.hasText(oauth2UserInfo.getImageUrl())) {
            user.updateProfile(user.getUsername(), oauth2UserInfo.getImageUrl());
        }

        log.info("사용자 로그인 성공: {}, 제공자: {}", user.getEmail(), authProvider);
        return userRepository.save(user);
    }

    private User registerNewUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 사용자명 정규화 및 고유값 생성 로직
        String originalName = oauth2UserInfo.getName();
        if (!StringUtils.hasText(originalName)) {
            originalName = oauth2UserInfo.getEmail().split("@")[0];
        }

        String normalizedName = normalizeUsername(originalName);
        String uniqueUsername = generateUniqueUsername(normalizedName);

        log.info("새 사용자 등록: {}, 제공자: {}, 사용자명: {}",
                oauth2UserInfo.getEmail(), authProvider, uniqueUsername);

        User user = User.builder()
                .provider(authProvider)
                .providerId(oauth2UserInfo.getId())
                .email(oauth2UserInfo.getEmail())
                .username(uniqueUsername)
                .profileImage(oauth2UserInfo.getImageUrl())
                .build();

        // 사용자 저장
        user = userRepository.save(user);
        
        // UserLevel 생성 및 저장 (자동)
        createUserLevel(user);

        return user;
    }
    
    /**
     * 사용자의 UserLevel 정보를 생성합니다.
     */
    private void createUserLevel(User user) {
        try {
            // LevelingService를 사용할 수 없으므로 직접 생성
            com.quizplatform.core.domain.user.UserLevel userLevel = new com.quizplatform.core.domain.user.UserLevel(user);
            
            // 로깅
            log.info("사용자 레벨 정보 생성: userId={}, level={}", user.getId(), userLevel.getLevel());
            
            // UserLevel 저장
            userRepository.flush(); // User를 먼저 flush하여 ID 확정
            userLevelRepository.save(userLevel);
        } catch (Exception e) {
            // 실패해도 사용자 생성은 계속 진행
            log.error("사용자 레벨 정보 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 사용자명을 정규화하는 메서드
     * 특수문자 제거 및 최대 길이 적용
     */
    private String normalizeUsername(String username) {
        if (username == null) {
            return "user";
        }

        // 특수문자 및 공백 제거 (영문, 숫자, 한글만 허용)
        String normalized = username.replaceAll("[^a-zA-Z0-9가-힣]", "");

        // 빈 문자열이 된 경우 기본값 사용
        if (normalized.isEmpty()) {
            normalized = "user";
        }

        // 최대 길이 제한 (20자)
        if (normalized.length() > 20) {
            normalized = normalized.substring(0, 20);
        }

        return normalized;
    }

    /**
     * 개선된 고유 사용자명 생성 메서드
     * 다양한 전략을 사용하여 중복을 해결
     */
    private String generateUniqueUsername(String baseName) {
        // 기본 이름이 이미 고유하면 그대로 사용
        if (!userRepository.existsByUsername(baseName)) {
            return baseName;
        }

        // 전략 1: 숫자 접미사 추가 (최대 100까지 시도)
        for (int i = 1; i <= 100; i++) {
            String candidateUsername = String.format("%s%d", baseName, i);
            // 이름 길이가 20자를 초과하는 경우 자르기
            if (candidateUsername.length() > 20) {
                // 숫자 자릿수에 따라 적절히 자르기
                int suffixLength = String.valueOf(i).length();
                candidateUsername = baseName.substring(0, 20 - suffixLength) + i;
            }

            if (!userRepository.existsByUsername(candidateUsername)) {
                return candidateUsername;
            }
        }

        // 전략 2: 랜덤 문자열 추가
        for (int attempt = 0; attempt < 5; attempt++) {
            String randomSuffix = generateRandomString(4);
            String candidateUsername = baseName;

            // 이름 길이가 20자를 초과하는 경우 자르기
            if (candidateUsername.length() + randomSuffix.length() > 20) {
                candidateUsername = baseName.substring(0, 20 - randomSuffix.length() - 1);
            }

            candidateUsername = candidateUsername + "_" + randomSuffix;

            if (!userRepository.existsByUsername(candidateUsername)) {
                return candidateUsername;
            }
        }

        // 최후의 수단: 타임스탬프 기반 사용자명
        String timeBasedUsername = baseName.substring(0, Math.min(10, baseName.length()))
                + "_" + System.currentTimeMillis() % 10000;

        // 최대 길이 제한
        if (timeBasedUsername.length() > 20) {
            timeBasedUsername = timeBasedUsername.substring(0, 20);
        }

        return timeBasedUsername;
    }

    /**
     * 지정된 길이의 랜덤 문자열 생성
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}