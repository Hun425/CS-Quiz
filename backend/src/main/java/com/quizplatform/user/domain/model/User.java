package com.quizplatform.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * User 도메인 모델 (순수 비즈니스 객체)
 */
@Getter
public class User {

    private Long id;
    private AuthProvider provider;
    private String providerId;
    private String email;
    private String username;
    private String profileImage;
    private UserRole role;
    private boolean isActive;
    private int totalPoints;
    private int level;
    private int experience;
    private int requiredExperience;
    // 토큰 관련 정보는 인증/세션 관리 책임으로 분리 고려 (여기서는 일단 포함)
    private String accessToken;
    private String refreshToken;
    private ZonedDateTime tokenExpiresAt;
    private ZonedDateTime lastLogin;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @Builder
    public User(Long id, AuthProvider provider, String providerId, String email, String username, String profileImage, UserRole role, boolean isActive, int totalPoints, int level, int experience, int requiredExperience, String accessToken, String refreshToken, ZonedDateTime tokenExpiresAt, ZonedDateTime lastLogin, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.username = username;
        this.profileImage = profileImage;
        this.role = (role == null) ? UserRole.USER : role; // 기본값 설정
        this.isActive = isActive;
        this.totalPoints = totalPoints;
        this.level = (level == 0) ? 1 : level; // 기본값 설정
        this.experience = experience;
        this.requiredExperience = (requiredExperience == 0) ? 100 : requiredExperience; // 기본값 설정
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- 비즈니스 로직 메소드 (UserJpaEntity에서 이동) ---

    /**
     * 경험치 획득 및 레벨업 처리
     *
     * @param exp 획득한 경험치
     * @return 레벨업 발생 여부
     */
    public boolean gainExperience(int exp) {
        this.experience += exp;
        this.totalPoints += exp; // 포인트도 같이 증가한다고 가정
        boolean leveledUp = false;
        while (this.experience >= this.requiredExperience) {
            levelUp();
            leveledUp = true;
        }
        return leveledUp;
    }

    /**
     * 레벨업 처리
     */
    private void levelUp() {
        this.level++;
        this.experience -= this.requiredExperience;
        this.requiredExperience = calculateNextLevelExp();
        // TODO: 레벨업 이벤트 발행 (필요시)
    }

    /**
     * 다음 레벨에 필요한 경험치 계산
     * (계산 로직은 UserJpaEntity의 것을 참고하여 구현 필요)
     * @return 필요 경험치
     */
    private int calculateNextLevelExp() {
        // 예시: 레벨 * 100 (실제 로직으로 대체 필요)
        return this.level * 100 + 100;
    }

    /**
     * 프로필 정보 업데이트
     * @param username 새 사용자명
     * @param profileImage 새 프로필 이미지 URL
     */
    public void updateProfile(String username, String profileImage) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username;
        }
        // 프로필 이미지는 null 허용 또는 기본 이미지 경로 설정 등 정책 필요
        this.profileImage = profileImage;
        this.updatedAt = ZonedDateTime.now(); // 업데이트 시간 갱신
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLogin = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

     /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = ZonedDateTime.now();
         // TODO: UserDeactivated 이벤트 발행 (필요시)
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = ZonedDateTime.now();
        // TODO: UserActivated 이벤트 발행 (필요시)
    }

    // TODO: updateLoginInfo, toggleActive, updateRole, updateProvider, addPoints 등
    //       필요한 비즈니스 로직 메소드 추가 구현
} 