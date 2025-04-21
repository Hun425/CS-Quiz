package com.quizplatform.modules.user.security.oauth;

import com.quizplatform.core.exception.OAuth2AuthenticationProcessingException;
import com.quizplatform.modules.security.oauth.CustomOAuth2UserService;
import com.quizplatform.modules.security.oauth.OAuth2UserInfo;
import com.quizplatform.modules.security.oauth.OAuth2UserInfoFactory;
import com.quizplatform.modules.user.domain.AuthProvider;
import com.quizplatform.modules.user.domain.User;
import com.quizplatform.modules.user.domain.UserLevel;
import com.quizplatform.modules.user.repository.UserLevelRepository;
import com.quizplatform.modules.user.repository.UserRepository;
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

/**
 * OAuth2 사용자 정보 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService {

    private final UserRepository userRepository;
    private final UserLevelRepository userLevelRepository;
    private final Random random = new Random();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            log.debug("OAuth2User loaded from provider: {}", oauth2User.getName());
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("OAuth2 인증 처리 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }

    @Override
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }

    /**
     * OAuth2 사용자 정보를 처리합니다.
     */
    @Transactional
    protected OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // 인증 제공자 식별
        AuthProvider authProvider = AuthProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );
        log.debug("Processing user from provider: {}", authProvider);

        // 사용자 정보 파싱
        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                authProvider,
                oauth2User.getAttributes()
        );
        log.debug("Parsed OAuth2UserInfo: email={}, name={}", oauth2UserInfo.getEmail(), oauth2UserInfo.getName());

        // 이메일 유효성 검증
        validateEmail(oauth2UserInfo.getEmail());

        // 사용자 처리
        User user = processUser(oauth2UserInfo, authProvider);

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        userRepository.save(user);
        log.debug("User last login updated for: {}", user.getEmail());

        // UserPrincipal 객체 생성
        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    /**
     * 이메일 유효성을 검증합니다.
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            log.error("Email not found from OAuth2 provider.");
            throw new OAuth2AuthenticationException("OAuth2 제공자에서 이메일을 찾을 수 없습니다");
        }
    }

    /**
     * 사용자 정보를 처리합니다.
     */
    @Transactional
    protected User processUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 이메일로 기존 사용자 검색
        Optional<User> userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail());

        if (userOptional.isPresent()) {
            // 기존 사용자 처리
            log.debug("Existing user found with email: {}", oauth2UserInfo.getEmail());
            return updateExistingUser(userOptional.get(), oauth2UserInfo, authProvider);
        } else {
            // 신규 사용자 처리
            log.debug("New user detected with email: {}", oauth2UserInfo.getEmail());
            return registerNewUser(oauth2UserInfo, authProvider);
        }
    }

    /**
     * 기존 사용자 정보를 업데이트합니다.
     */
    private User updateExistingUser(User user, OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 사용자가 이전에 다른 Provider로 가입했는지 확인
        if (!user.getProvider().equals(authProvider)) {
            log.info("Updating user provider: {} -> {}. User: {}",
                    user.getProvider(), authProvider, user.getEmail());
            // Provider 정보 및 Provider ID 업데이트
            user.updateProvider(authProvider, oauth2UserInfo.getId());
        }

        // 프로필 이미지 업데이트
        if (StringUtils.hasText(oauth2UserInfo.getImageUrl()) &&
                !oauth2UserInfo.getImageUrl().equals(user.getProfileImage())) {
            log.info("Updating profile image for user: {}", user.getEmail());
            user.updateProfile(user.getUsername(), oauth2UserInfo.getImageUrl());
        }

        log.info("User '{}' logged in successfully via {}.", user.getEmail(), authProvider);
        return userRepository.save(user);
    }

    /**
     * 신규 사용자를 등록합니다.
     */
    private User registerNewUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 사용자명 결정
        String originalName = oauth2UserInfo.getName();
        if (!StringUtils.hasText(originalName)) {
            originalName = oauth2UserInfo.getEmail().split("@")[0];
        }

        // 사용자명 정규화
        String normalizedName = normalizeUsername(originalName);
        // 고유 사용자명 생성
        String uniqueUsername = generateUniqueUsername(normalizedName);

        log.info("Registering new user: email={}, provider={}, generatedUsername={}",
                oauth2UserInfo.getEmail(), authProvider, uniqueUsername);

        // User 엔티티 생성
        User user = User.builder()
                .provider(authProvider)
                .providerId(oauth2UserInfo.getId())
                .email(oauth2UserInfo.getEmail())
                .username(uniqueUsername)
                .profileImage(oauth2UserInfo.getImageUrl())
                .build();

        // User 엔티티 저장
        user = userRepository.save(user);

        // 초기 UserLevel 정보 생성 및 저장
        createUserLevel(user);

        return user;
    }

    /**
     * 초기 UserLevel을 생성합니다.
     */
    private void createUserLevel(User user) {
        try {
            // UserLevel 존재 여부 확인
            if (!userLevelRepository.findByUserId(user.getId()).isPresent()) {
                // UserLevel 객체 생성
                UserLevel userLevel = new UserLevel(user);

                log.info("Creating initial UserLevel for new user: userId={}, initialLevel={}", user.getId(), userLevel.getLevel());

                // User 엔티티 플러시
                userRepository.flush();
                // UserLevel 저장
                userLevelRepository.save(userLevel);
            } else {
                log.warn("UserLevel already exists for userId: {}", user.getId());
            }
        } catch (Exception e) {
            log.error("Failed to create UserLevel for userId {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    /**
     * 사용자명을 정규화합니다.
     */
    private String normalizeUsername(String username) {
        if (username == null) {
            return "user";
        }

        // 허용되지 않는 문자 제거
        String normalized = username.replaceAll("[^a-zA-Z0-9가-힣]", "");

        // 빈 문자열이면 기본값 사용
        if (normalized.isEmpty()) {
            normalized = "user";
        }

        // 최대 길이(20자) 제한
        if (normalized.length() > 20) {
            normalized = normalized.substring(0, 20);
        }

        return normalized;
    }

    /**
     * 고유한 사용자명을 생성합니다.
     */
    private String generateUniqueUsername(String baseName) {
        // 기본 이름이 이미 고유한지 확인
        if (!userRepository.existsByUsername(baseName)) {
            log.debug("Generated unique username (using base name): {}", baseName);
            return baseName;
        }

        // 숫자 접미사 추가 시도
        for (int i = 1; i <= 100; i++) {
            String candidateUsername = baseName + i;
            // 최종 길이가 20자를 초과하는 경우, baseName을 줄여서 길이 맞춤
            if (candidateUsername.length() > 20) {
                int suffixLength = String.valueOf(i).length();
                if (20 - suffixLength > 0) {
                    candidateUsername = baseName.substring(0, 20 - suffixLength) + i;
                } else {
                    candidateUsername = String.valueOf(i).substring(0, 20);
                }
            }
            // 생성된 이름이 고유한지 확인
            if (!userRepository.existsByUsername(candidateUsername)) {
                log.debug("Generated unique username (with numeric suffix): {}", candidateUsername);
                return candidateUsername;
            }
        }

        // 랜덤 문자열 접미사 추가 시도
        for (int attempt = 0; attempt < 5; attempt++) {
            String randomSuffix = generateRandomString(4);
            String candidatePrefix = baseName;

            // 접미사 추가 시 길이가 20자를 넘는지 확인하고 prefix 길이 조절
            if (candidatePrefix.length() + randomSuffix.length() + 1 > 20) {
                if (20 - randomSuffix.length() - 1 > 0) {
                    candidatePrefix = baseName.substring(0, 20 - randomSuffix.length() - 1);
                } else {
                    candidatePrefix = "";
                }
            }
            String candidateUsername = candidatePrefix.isEmpty() ? randomSuffix : candidatePrefix + "_" + randomSuffix;

            // 생성된 이름이 고유한지 확인
            if (!userRepository.existsByUsername(candidateUsername)) {
                log.debug("Generated unique username (with random suffix): {}", candidateUsername);
                return candidateUsername;
            }
        }

        // 타임스탬프 기반 이름 생성
        String timeBasedUsername = baseName.substring(0, Math.min(10, baseName.length()))
                + "_" + System.currentTimeMillis() % 10000;
        // 최대 길이 제한 적용
        if (timeBasedUsername.length() > 20) {
            timeBasedUsername = timeBasedUsername.substring(0, 20);
        }
        log.warn("Failed to generate unique username with numeric/random suffix, using time-based fallback: {}", timeBasedUsername);
        return timeBasedUsername;
    }

    /**
     * 랜덤 문자열을 생성합니다.
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}