package com.quizplatform.common.adapter.config.security.oauth;

// TODO: UserPrincipal, AuthProvider, User, UserRepository, UserLevelRepository, OAuth2AuthenticationProcessingException 등의 최종 위치 확인 후 import 경로 재검토 필요
import com.quizplatform.common.adapter.config.security.UserPrincipal;
import com.quizplatform.common.exception.OAuth2AuthenticationProcessingException; // common/exception 으로 이동 가정
import com.quizplatform.user.domain.AuthProvider; // user/domain 으로 이동 가정
import com.quizplatform.user.domain.User; // user/domain 으로 이동 가정
import com.quizplatform.user.domain.UserLevel; // user/domain 으로 이동 가정
import com.quizplatform.user.domain.out.persistence.UserLevelRepository; // user 모듈의 persistence adapter 로 이동 가정 (Port/Interface 사용 고려)
import com.quizplatform.user.domain.out.persistence.UserRepository; // user 모듈의 persistence adapter 로 이동 가정 (Port/Interface 사용 고려)
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
    // TODO: UserRepository, UserLevelRepository 를 직접 주입하는 대신 User 도메인의 Port 인터페이스를 주입받도록 리팩토링 고려
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
            // TODO: 공통 Exception 클래스로 변경 (OAuth2AuthenticationProcessingException 사용 검토)
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
        // TODO: AuthProvider enum 위치 확인
        AuthProvider authProvider = AuthProvider.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );
        log.debug("Processing user from provider: {}", authProvider);

        // 2. 제공자별 사용자 정보 파싱 (OAuth2UserInfoFactory 사용 - 동일 패키지)
        OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                authProvider,
                oauth2User.getAttributes()
        );
        log.debug("Parsed OAuth2UserInfo: email={}, name={}", oauth2UserInfo.getEmail(), oauth2UserInfo.getName());

        // 3. 이메일 유효성 검증 (필수 정보)
        validateEmail(oauth2UserInfo.getEmail());

        // 4. DB에서 이메일 기반으로 사용자 조회 또는 신규 등록/업데이트 처리
        // TODO: User 엔티티 위치 확인
        User user = processUser(oauth2UserInfo, authProvider);

        // 5. 사용자의 마지막 로그인 시간 업데이트
        user.updateLastLogin();
        // TODO: UserRepository 위치 확인 (Port 사용 고려)
        userRepository.save(user); // 변경 사항 저장
        log.debug("User last login updated for: {}", user.getEmail());

        // 6. 최종적으로 UserPrincipal 객체 생성하여 반환 (Spring Security에서 사용)
        // UserPrincipal은 UserDetails와 OAuth2User 인터페이스를 모두 구현 (같은 패키지)
        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    /**
     * OAuth2 제공자로부터 받은 이메일 주소가 유효한지(null 또는 비어있지 않은지) 확인합니다. (내부 헬퍼 메서드)
     *
     * @param email 검증할 이메일 주소
     * @throws OAuth2AuthenticationProcessingException 이메일이 유효하지 않은 경우 (TODO: 예외 타입 확인)
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            log.error("Email not found from OAuth2 provider.");
            // TODO: 공통 Exception 타입 사용
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
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
        // TODO: UserRepository 위치 확인 (Port 사용 고려)
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
        // TODO: AuthProvider 위치 확인
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
        // TODO: UserRepository 위치 확인 (Port 사용 고려)
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
        // TODO: User, AuthProvider 위치 확인
        User user = User.builder()
                .provider(authProvider)
                .providerId(oauth2UserInfo.getId())
                .email(oauth2UserInfo.getEmail())
                .username(uniqueUsername) // 고유값으로 생성된 사용자명 사용
                .profileImage(oauth2UserInfo.getImageUrl())
                // .role(Role.USER) // 기본 역할 설정 (필요 시)
                .build();

        // 5. User 엔티티 저장
        // TODO: UserRepository 위치 확인 (Port 사용 고려)
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
            // TODO: UserLevelRepository, UserLevel 위치 확인 (Port 사용 고려)
            if (!userLevelRepository.findByUserId(user.getId()).isPresent()) {
                // UserLevel 객체 생성 (기본 레벨 1, 경험치 0)
                UserLevel userLevel = new UserLevel(user);

                log.info("Creating initial UserLevel for new user: userId={}, initialLevel={}", user.getId(), userLevel.getLevel());

                // User 엔티티를 먼저 flush하여 DB에 ID가 확실히 반영되도록 함 (필요 시)
                // TODO: UserRepository 위치 확인 (Port 사용 고려)
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
     * 사용자 이름을 정규화하여 반환합니다.
     * 소문자로 변환하고, 영어 알파벳, 숫자, 하이픈(-), 언더스코어(_)만 남깁니다.
     * 길이가 3자 미만이거나 20자를 초과하면 적절히 조정합니다.
     * (내부 헬퍼 메서드)
     *
     * @param username 정규화할 원본 사용자 이름
     * @return 정규화된 사용자 이름
     */
    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return "user"; // 기본값
        }
        String normalized = username.toLowerCase()
                .replaceAll("[^a-z0-9_-]", "") // 허용 문자 외 제거
                .replaceAll("^-+", "") // 시작 하이픈 제거
                .replaceAll("-+$", ""); // 끝 하이픈 제거

        if (normalized.length() < 3) {
            normalized = normalized + generateRandomString(3 - normalized.length());
        } else if (normalized.length() > 20) {
            normalized = normalized.substring(0, 20);
        }
        // 최종적으로 유효하지 않은 문자(예: 모두 하이픈) 처리
        if (!normalized.matches(".*[a-z0-9].*")) {
            return "user" + generateRandomString(3);
        }
        return normalized;
    }

    /**
     * 주어진 기본 이름을 바탕으로 고유한 사용자명을 생성합니다.
     * DB에서 해당 이름 또는 패턴(basename + 숫자)으로 시작하는 사용자가 있는지 확인하고,
     * 중복되지 않을 때까지 숫자를 증가시키거나 랜덤 문자열을 추가합니다.
     * 최대 100번 시도 후 실패 시 예외를 발생시킵니다.
     * (내부 처리 로직)
     *
     * @param baseName 정규화된 기본 사용자명
     * @return DB에 존재하지 않는 고유한 사용자명
     * @throws IllegalStateException 고유 사용자명 생성 실패 시 (100번 시도 후)
     */
    private String generateUniqueUsername(String baseName) {
        String potentialUsername = baseName;
        int attempt = 0;
        final int maxAttempts = 100; // 최대 시도 횟수

        // TODO: UserRepository 위치 확인 (Port 사용 고려)
        while (userRepository.existsByUsername(potentialUsername)) {
            attempt++;
            if (attempt >= maxAttempts) {
                // 최대 시도 횟수 초과 시 랜덤 문자열 추가 또는 예외 발생
                log.error("Failed to generate unique username for base '{}' after {} attempts.", baseName, maxAttempts);
                potentialUsername = baseName + generateRandomString(5); // 마지막 시도
                if (userRepository.existsByUsername(potentialUsername)) {
                    throw new IllegalStateException("Could not generate a unique username for: " + baseName);
                }
                break; // 루프 종료
            }
            // 숫자를 증가시켜 새로운 사용자명 시도 (예: user1, user2, ...)
            potentialUsername = baseName + attempt;
            log.trace("Username '{}' exists, trying '{}' (attempt {}).", baseName + (attempt > 1 ? (attempt - 1) : ""), potentialUsername, attempt);
        }
        log.debug("Generated unique username: {}", potentialUsername);
        return potentialUsername;
    }

    /**
     * 지정된 길이의 랜덤 문자열(소문자 알파벳)을 생성합니다. (내부 헬퍼 메서드)
     *
     * @param length 생성할 문자열 길이
     * @return 랜덤 소문자 알파벳 문자열
     */
    private String generateRandomString(int length) {
        if (length <= 0) return "";
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 사용자 ID를 기반으로 UserDetails 객체를 로드합니다.
     * 이 메서드는 UserDetailsService 인터페이스 구현을 위한 것이 아니라,
     * 내부적으로 사용자 ID를 통해 UserPrincipal을 로드해야 할 때 사용될 수 있습니다.
     * (예: JWT 토큰 검증 후 사용자 정보 로드 시)
     *
     * @param id 로드할 사용자의 ID
     * @return 해당 ID를 가진 사용자의 UserPrincipal 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserDetails loadUserById(Long id) {
        // TODO: UserRepository 위치 확인 (Port 사용 고려)
        User user = userRepository.findById(id).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id : " + id)
        );
        // TODO: UserPrincipal 위치 확인
        return UserPrincipal.create(user);
    }

} 