package com.quizplatform.core.config.security.oauth;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.repository.UserRepository;
// UserLevelRepository import 추가
import com.quizplatform.core.repository.user.UserLevelRepository;
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
 * Spring Security의 DefaultOAuth2UserService를 확장하여 OAuth2 로그인 시
 * 사용자 정보를 커스텀하게 처리하는 서비스입니다.
 * 외부 OAuth2 제공자로부터 사용자 정보를 받아와서, 기존 사용자인 경우 정보를 업데이트하거나,
 * 신규 사용자인 경우 새로 등록하는 로직을 수행합니다.
 * 사용자 이름 정규화 및 고유값 생성, UserLevel 초기 생성 등의 추가 작업을 처리합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Service
@RequiredArgsConstructor
@Transactional // 클래스 레벨 트랜잭션 적용 (메서드별 재정의 가능)
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final UserLevelRepository userLevelRepository; // UserLevel 생성을 위해 주입
    private final Random random = new Random(); // 고유 사용자명 생성 시 사용

    /**
     * OAuth2 제공자로부터 사용자 정보를 로드하는 기본 메서드를 오버라이드합니다.
     * 부모 클래스의 loadUser를 호출하여 기본적인 OAuth2User 정보를 가져온 후,
     * processOAuth2User 메서드를 통해 커스텀 로직을 수행합니다.
     *
     * @param userRequest OAuth2 사용자 정보 요청 객체
     * @return 처리된 OAuth2User 객체 (내부적으로 UserPrincipal 사용)
     * @throws OAuth2AuthenticationException 인증 처리 중 오류 발생 시
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // 1. 부모 클래스(DefaultOAuth2UserService)의 loadUser 호출하여 기본 OAuth2User 정보 로드
            OAuth2User oauth2User = super.loadUser(userRequest);
            log.debug("OAuth2User loaded from provider: {}", oauth2User.getName());
            // 2. 로드된 OAuth2User 정보와 요청 정보를 바탕으로 커스텀 처리 로직 수행
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            // 처리 중 발생한 예외 로깅 및 OAuth2AuthenticationException으로 변환하여 던짐
            log.error("Error processing OAuth2 user: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("OAuth2 authentication processing failed: " + ex.getMessage());
        }
    }

    /**
     * 로드된 OAuth2 사용자 정보(oauth2User)를 기반으로 시스템의 User 엔티티를 처리합니다.
     * 제공자(Provider) 확인, 사용자 정보 추출, 이메일 검증, 기존 사용자 확인/업데이트 또는 신규 등록,
     * 마지막 로그인 시간 업데이트 등의 작업을 수행합니다.
     * 최종적으로 UserPrincipal 객체를 생성하여 반환합니다. (내부 처리 로직)
     *
     * @param userRequest OAuth2 사용자 정보 요청 객체
     * @param oauth2User  OAuth2 제공자로부터 로드된 사용자 정보
     * @return 시스템 User 정보와 OAuth2 속성을 포함하는 UserPrincipal 객체
     */
    @Transactional // 메서드 레벨 트랜잭션 명시
    protected OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // 1. OAuth2 제공자(Provider) 식별 (예: GOOGLE, KAKAO)
        AuthProvider authProvider = AuthProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );
        log.debug("Processing user from provider: {}", authProvider);

        // 2. 제공자별 사용자 정보 파싱 (OAuth2UserInfoFactory 사용)
        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                authProvider,
                oauth2User.getAttributes()
        );
        log.debug("Parsed OAuth2UserInfo: email={}, name={}", oauth2UserInfo.getEmail(), oauth2UserInfo.getName());

        // 3. 이메일 유효성 검증 (필수 정보)
        validateEmail(oauth2UserInfo.getEmail());

        // 4. DB에서 이메일 기반으로 사용자 조회 또는 신규 등록/업데이트 처리
        User user = processUser(oauth2UserInfo, authProvider);

        // 5. 사용자의 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        userRepository.save(user); // 변경 사항 저장
        log.debug("User last login updated for: {}", user.getEmail());

        // 6. 최종적으로 UserPrincipal 객체 생성하여 반환 (Spring Security에서 사용)
        // UserPrincipal은 UserDetails와 OAuth2User 인터페이스를 모두 구현
        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    /**
     * OAuth2 제공자로부터 받은 이메일 주소가 유효한지(null 또는 비어있지 않은지) 확인합니다. (내부 헬퍼 메서드)
     *
     * @param email 검증할 이메일 주소
     * @throws OAuth2AuthenticationException 이메일이 유효하지 않은 경우
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            log.error("Email not found from OAuth2 provider.");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
    }

    /**
     * 주어진 OAuth2 사용자 정보를 바탕으로 기존 사용자를 업데이트하거나 신규 사용자를 등록합니다. (내부 처리 로직)
     *
     * @param oauth2UserInfo 파싱된 OAuth2 사용자 정보
     * @param authProvider   로그인 시 사용된 OAuth2 제공자
     * @return 업데이트되거나 새로 등록된 User 엔티티
     */
    @Transactional // 메서드 레벨 트랜잭션 명시
    protected User processUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 이메일로 기존 사용자 검색
        Optional<User> userOptional = userRepository.findByEmail(oauth2UserInfo.getEmail());

        if (userOptional.isPresent()) {
            // --- 기존 사용자 처리 ---
            log.debug("Existing user found with email: {}", oauth2UserInfo.getEmail());
            // 기존 사용자 정보 업데이트 로직 호출
            return updateExistingUser(userOptional.get(), oauth2UserInfo, authProvider);
        } else {
            // --- 신규 사용자 처리 ---
            log.debug("New user detected with email: {}", oauth2UserInfo.getEmail());
            // 신규 사용자 등록 로직 호출
            return registerNewUser(oauth2UserInfo, authProvider);
        }
    }

    /**
     * 기존 사용자의 정보를 OAuth2 로그인 정보로 업데이트합니다. (내부 헬퍼 메서드)
     * 다른 소셜 계정으로 이미 가입된 경우, 현재 로그인한 제공자 정보로 업데이트합니다.
     * 프로필 이미지가 변경된 경우 업데이트합니다. (사용자명은 기존 값 유지)
     *
     * @param user           기존 User 엔티티
     * @param oauth2UserInfo 새로 로그인한 OAuth2 사용자 정보
     * @param authProvider   새로 로그인한 OAuth2 제공자
     * @return 업데이트된 User 엔티티
     */
    private User updateExistingUser(User user, OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 사용자가 이전에 다른 Provider로 가입했는지 확인
        if (!user.getProvider().equals(authProvider)) {
            log.info("Updating user provider: {} -> {}. User: {}",
                    user.getProvider(), authProvider, user.getEmail());
            // Provider 정보 및 Provider ID 업데이트
            user.updateProvider(authProvider, oauth2UserInfo.getId());
        }

        // 프로필 이미지 업데이트 (OAuth2 제공 정보에 이미지 URL이 있고, 기존과 다를 경우)
        if (StringUtils.hasText(oauth2UserInfo.getImageUrl()) &&
                !oauth2UserInfo.getImageUrl().equals(user.getProfileImage())) {
            log.info("Updating profile image for user: {}", user.getEmail());
            // 사용자명은 변경하지 않고 프로필 이미지만 업데이트
            user.updateProfile(user.getUsername(), oauth2UserInfo.getImageUrl());
        }

        log.info("User '{}' logged in successfully via {}.", user.getEmail(), authProvider);
        // 변경 사항 저장 (JPA dirty checking으로 자동 저장될 수도 있지만 명시적 호출)
        return userRepository.save(user);
    }

    /**
     * OAuth2 사용자 정보를 기반으로 신규 사용자를 등록합니다. (내부 헬퍼 메서드)
     * 사용자명을 정규화하고 중복되지 않도록 고유한 사용자명을 생성합니다.
     * User 엔티티를 생성 및 저장하고, 초기 UserLevel 정보도 생성합니다.
     *
     * @param oauth2UserInfo 등록할 OAuth2 사용자 정보
     * @param authProvider   등록 시 사용된 OAuth2 제공자
     * @return 새로 등록된 User 엔티티
     */
    private User registerNewUser(OAuth2UserInfo oauth2UserInfo, AuthProvider authProvider) {
        // 1. 사용자명 결정 (OAuth2 이름 -> 없으면 이메일 앞부분)
        String originalName = oauth2UserInfo.getName();
        if (!StringUtils.hasText(originalName)) {
            originalName = oauth2UserInfo.getEmail().split("@")[0];
        }

        // 2. 사용자명 정규화 (특수문자 제거, 길이 제한 등)
        String normalizedName = normalizeUsername(originalName);
        // 3. 고유 사용자명 생성 (중복 시 숫자 또는 랜덤 문자 추가)
        String uniqueUsername = generateUniqueUsername(normalizedName);

        log.info("Registering new user: email={}, provider={}, generatedUsername={}",
                oauth2UserInfo.getEmail(), authProvider, uniqueUsername);

        // 4. User 엔티티 생성
        User user = User.builder()
                .provider(authProvider)
                .providerId(oauth2UserInfo.getId())
                .email(oauth2UserInfo.getEmail())
                .username(uniqueUsername) // 고유값으로 생성된 사용자명 사용
                .profileImage(oauth2UserInfo.getImageUrl())
                // .role(Role.USER) // 기본 역할 설정 (필요 시)
                .build();

        // 5. User 엔티티 저장
        user = userRepository.save(user);

        // 6. 초기 UserLevel 정보 생성 및 저장
        createUserLevel(user);

        return user;
    }

    /**
     * 새로 등록된 사용자에 대한 초기 UserLevel 객체를 생성하고 저장합니다. (내부 헬퍼 메서드)
     * LevelingService 의존성 없이 직접 생성합니다.
     *
     * @param user UserLevel을 생성할 대상 User 엔티티 (ID가 할당된 상태여야 함)
     */
    private void createUserLevel(User user) {
        try {
            // UserLevelRepository를 사용하여 이미 존재하는지 확인 (중복 생성 방지)
            if (!userLevelRepository.findByUserId(user.getId()).isPresent()) {
                // UserLevel 객체 생성 (기본 레벨 1, 경험치 0)
                com.quizplatform.core.domain.user.UserLevel userLevel = new com.quizplatform.core.domain.user.UserLevel(user);

                log.info("Creating initial UserLevel for new user: userId={}, initialLevel={}", user.getId(), userLevel.getLevel());

                // User 엔티티를 먼저 flush하여 DB에 ID가 확실히 반영되도록 함 (필요 시)
                userRepository.flush();
                // UserLevel 저장
                userLevelRepository.save(userLevel);
            } else {
                log.warn("UserLevel already exists for userId: {}", user.getId());
            }
        } catch (Exception e) {
            // UserLevel 생성 실패는 사용자 등록 자체를 롤백시키지 않도록 처리
            log.error("Failed to create UserLevel for userId {}: {}", user.getId(), e.getMessage(), e);
            // 여기서 예외를 다시 던지지 않음
        }
    }

    /**
     * 사용자명을 정규화합니다. (내부 헬퍼 메서드)
     * 영문, 숫자, 한글을 제외한 특수문자 및 공백을 제거하고 최대 길이(20자)를 적용합니다.
     * 정규화 후 빈 문자열이 되면 "user"를 기본값으로 사용합니다.
     *
     * @param username 원본 사용자명 문자열
     * @return 정규화된 사용자명 문자열
     */
    private String normalizeUsername(String username) {
        if (username == null) {
            return "user"; // null 입력 시 기본값 반환
        }

        // 정규표현식을 사용하여 허용되지 않는 문자 제거
        String normalized = username.replaceAll("[^a-zA-Z0-9가-힣]", "");

        // 결과가 빈 문자열이면 기본값 사용
        if (normalized.isEmpty()) {
            normalized = "user";
        }

        // 최대 길이(20자) 제한 적용
        if (normalized.length() > 20) {
            normalized = normalized.substring(0, 20);
        }

        return normalized;
    }

    /**
     * 주어진 기본 사용자명(baseName)에 대해 데이터베이스에서 고유한 사용자명을 생성합니다. (내부 헬퍼 메서드)
     * 1. 기본 이름 사용 시도
     * 2. 기본 이름 + 숫자 접미사 (1~100) 시도 (길이 제한 고려)
     * 3. 기본 이름 + "_" + 랜덤 문자열(4자) 시도 (최대 5회, 길이 제한 고려)
     * 4. 최후의 수단: 기본 이름 일부 + "_" + 타임스탬프 일부 사용
     *
     * @param baseName 고유 사용자명을 생성하기 위한 기본 이름
     * @return 데이터베이스 내에서 고유한 사용자명 문자열
     */
    private String generateUniqueUsername(String baseName) {
        // 1. 기본 이름이 이미 고유한지 확인
        if (!userRepository.existsByUsername(baseName)) {
            log.debug("Generated unique username (using base name): {}", baseName);
            return baseName;
        }

        // 2. 숫자 접미사 추가 시도 (최대 100번)
        for (int i = 1; i <= 100; i++) {
            String candidateUsername = baseName + i;
            // 최종 길이가 20자를 초과하는 경우, baseName을 줄여서 길이 맞춤
            if (candidateUsername.length() > 20) {
                int suffixLength = String.valueOf(i).length();
                if (20 - suffixLength > 0) { // baseName 부분이 남는지 확인
                    candidateUsername = baseName.substring(0, 20 - suffixLength) + i;
                } else { // 접미사만으로도 20자를 넘는 극단적인 경우 (사실상 발생 어려움)
                    candidateUsername = String.valueOf(i).substring(0, 20);
                }
            }
            // 생성된 이름이 고유한지 확인
            if (!userRepository.existsByUsername(candidateUsername)) {
                log.debug("Generated unique username (with numeric suffix): {}", candidateUsername);
                return candidateUsername;
            }
        }

        // 3. 랜덤 문자열 접미사 추가 시도 (최대 5번)
        for (int attempt = 0; attempt < 5; attempt++) {
            String randomSuffix = generateRandomString(4); // 4자리 랜덤 문자열 생성
            String candidatePrefix = baseName;

            // 접미사 추가 시 길이가 20자를 넘는지 확인하고 prefix 길이 조절
            if (candidatePrefix.length() + randomSuffix.length() + 1 > 20) { // +1은 '_' 고려
                if (20 - randomSuffix.length() - 1 > 0) {
                    candidatePrefix = baseName.substring(0, 20 - randomSuffix.length() - 1);
                } else { // prefix 없이 랜덤 문자열만 사용해야 하는 경우
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

        // 4. 최후의 수단: 타임스탬프 기반 이름 생성
        // baseName 앞부분(최대 10자) + "_" + 현재 시간 밀리초의 마지막 4자리
        String timeBasedUsername = baseName.substring(0, Math.min(10, baseName.length()))
                + "_" + System.currentTimeMillis() % 10000;
        // 최대 길이 제한 적용
        if (timeBasedUsername.length() > 20) {
            timeBasedUsername = timeBasedUsername.substring(0, 20);
        }
        log.warn("Failed to generate unique username with numeric/random suffix, using time-based fallback: {}", timeBasedUsername);
        // 이 이름도 중복될 가능성이 극히 낮지만, 엄밀히는 확인 후 반환해야 함 (여기서는 그냥 반환)
        return timeBasedUsername;
    }

    /**
     * 지정된 길이의 랜덤 영문 대/소문자 및 숫자 조합 문자열을 생성합니다. (내부 헬퍼 메서드)
     *
     * @param length 생성할 문자열의 길이
     * @return 생성된 랜덤 문자열
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

    /**
     * 사용자 ID를 기반으로 사용자 정보를 로드하여 UserDetails 객체로 반환합니다.
     * (CustomUserDetailsService에도 유사한 메서드가 존재함)
     *
     * @param id 조회할 사용자의 ID
     * @return Spring Security UserDetails 객체 (UserPrincipal)
     * @throws UsernameNotFoundException 해당 ID의 사용자를 찾을 수 없는 경우
     */
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}