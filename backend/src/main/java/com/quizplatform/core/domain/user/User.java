package com.quizplatform.core.domain.user;

import com.quizplatform.core.domain.quiz.QuizAttempt;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티 클래스
 * 
 * <p>퀴즈 플랫폼의 사용자 정보를 관리합니다.
 * 개인 정보, 인증 정보, 레벨, 경험치, 권한 등 사용자와 관련된 모든 데이터를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "users")
@EntityListeners({AuditingEntityListener.class})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    /**
     * 사용자 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자의 배틀 통계 정보
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserBattleStats battleStats;

    /**
     * 인증 제공자 (GOOGLE, GITHUB, KAKAO 등)
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    /**
     * 인증 제공자에서의 사용자 ID
     */
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    /**
     * 이메일 주소
     */
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    /**
     * 사용자명
     */
    @Column(nullable = false, length = 50, unique = true)
    private String username;

    /**
     * 프로필 이미지 URL
     */
    @Column(name = "profile_image")
    private String profileImage;

    /**
     * 사용자 권한
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * 계정 활성화 상태
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    /**
     * 총 획득 포인트
     */
    @Column(name = "total_points", nullable = false)
    private int totalPoints;

    /**
     * 현재 레벨
     */
    @Column(nullable = false)
    private int level;

    /**
     * 현재 경험치
     */
    @Column(nullable = false)
    private int experience;

    /**
     * 사용자의 퀴즈 시도 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    /**
     * 다음 레벨에 필요한 경험치
     */
    @Column(name = "required_experience", nullable = false)
    private int requiredExperience;

    /**
     * 액세스 토큰
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    /**
     * 리프레시 토큰
     */
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    /**
     * 토큰 만료 시간
     */
    @Column(name = "token_expires_at")
    private ZonedDateTime tokenExpiresAt;

    /**
     * 마지막 로그인 시간
     */
    @Column(name = "last_login")
    private ZonedDateTime lastLogin;

    /**
     * 계정 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    /**
     * 계정 정보 최종 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    /**
     * 엔티티 생성 전 호출되는 메서드
     * 
     * <p>기본값 설정 및 시간 초기화를 수행합니다.</p>
     */
    @PrePersist
    protected void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) {
            this.role = UserRole.USER;
        }
        // 기본값 설정
        if (this.totalPoints == 0) {
            this.totalPoints = 0;
        }
        if (this.level == 0) {
            this.level = 1;
        }
        if (this.experience == 0) {
            this.experience = 0;
        }
        if (this.requiredExperience == 0) {
            this.requiredExperience = 100;
        }
        // 기본 활성화 상태
        this.isActive = true;
    }

    /**
     * 엔티티 업데이트 전 호출되는 메서드
     * 
     * <p>업데이트 시간을 현재 시간으로 설정합니다.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * 사용자 생성자
     * 
     * @param provider 인증 제공자
     * @param providerId 제공자에서의 사용자 ID
     * @param email 이메일
     * @param username 사용자명
     * @param profileImage 프로필 이미지 URL
     */
    @Builder
    public User(AuthProvider provider, String providerId, String email, String username, String profileImage) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.username = username;
        this.profileImage = profileImage;
        this.role = UserRole.USER;
        this.isActive = true;
        this.totalPoints = 0;
        this.level = 1;
        this.experience = 0;
        this.requiredExperience = 100;
    }

    /**
     * 경험치 획득 및 레벨업 처리
     * 
     * @param exp 획득한 경험치
     */
    public void gainExperience(int exp) {
        this.experience += exp;
        this.totalPoints += exp;
        while (this.experience >= this.requiredExperience) {
            levelUp();
        }
    }

    /**
     * 레벨업 처리
     */
    private void levelUp() {
        this.level++;
        this.experience -= this.requiredExperience;
        this.requiredExperience = calculateNextLevelExp();
    }

    /**
     * 다음 레벨에 필요한 경험치 계산
     * 
     * @return 필요 경험치
     */
    private int calculateNextLevelExp() {
        return (int) (this.requiredExperience * 1.5);
    }

    /**
     * 로그인 정보 업데이트
     * 
     * @param accessToken 새 액세스 토큰
     * @param refreshToken 새 리프레시 토큰
     * @param expiresAt 토큰 만료 시간
     */
    public void updateLoginInfo(String accessToken, String refreshToken, ZonedDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
        this.lastLogin = ZonedDateTime.now();
    }

    /**
     * 계정 활성화 상태 토글
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    /**
     * 사용자 권한 변경
     * 
     * @param newRole 새 권한
     */
    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    /**
     * 프로필 정보 업데이트
     * 
     * @param username 새 사용자명
     * @param profileImage 새 프로필 이미지 URL
     */
    public void updateProfile(String username, String profileImage) {
        if (StringUtils.hasText(username)) {
            this.username = username;
        }
        if (StringUtils.hasText(profileImage)) {
            this.profileImage = profileImage;
        }
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * 인증 제공자 정보 업데이트
     * 
     * @param provider 새 인증 제공자
     * @param providerId 새 제공자 ID
     */
    public void updateProvider(AuthProvider provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLogin = ZonedDateTime.now();
    }

    /**
     * 포인트 추가
     * 
     * @param points 추가할 포인트
     * @throws IllegalArgumentException 음수 포인트 추가 시도시
     */
    public void addPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("포인트는 음수일 수 없습니다.");
        }
        this.totalPoints += points;
    }

    /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.isActive = true;
    }
}