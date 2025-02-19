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
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners({AuditingEntityListener.class})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "total_points", nullable = false)
    private int totalPoints;

    // 레벨 관련 필드 (첫 번째 엔티티 기능)
    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int experience;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    @Column(name = "required_experience", nullable = false)
    private int requiredExperience;

    // 토큰 및 로그인 관련 필드 (첫 번째 엔티티 기능)
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private ZonedDateTime tokenExpiresAt;

    @Column(name = "last_login")
    private ZonedDateTime lastLogin;

    // 생성/수정 시간 (두 엔티티 공통)
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // @PrePersist / @PreUpdate를 통해 기본값 및 시간 업데이트 처리 (두 번째 엔티티 방식 추가)
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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    // Builder 생성자 (기본 값 할당 포함)
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

    // [첫 번째 엔티티] 경험치 및 레벨업 관련 메서드
    public void gainExperience(int exp) {
        this.experience += exp;
        this.totalPoints += exp;
        while (this.experience >= this.requiredExperience) {
            levelUp();
        }
    }

    private void levelUp() {
        this.level++;
        this.experience -= this.requiredExperience;
        this.requiredExperience = calculateNextLevelExp();
    }

    private int calculateNextLevelExp() {
        return (int) (this.requiredExperience * 1.5);
    }

    // [첫 번째 엔티티] 로그인 및 토큰 업데이트 메서드
    public void updateLoginInfo(String accessToken, String refreshToken, ZonedDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
        this.lastLogin = ZonedDateTime.now();
    }

    // [첫 번째 엔티티] 활성화 토글, 권한 변경 메서드
    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    public void updateRole(UserRole newRole) {
        this.role = newRole;
    }

    // [두 번째 엔티티] 프로필 업데이트 메서드
    public void updateProfile(String username, String profileImage) {
        if (StringUtils.hasText(username)) {
            this.username = username;
        }
        if (StringUtils.hasText(profileImage)) {
            this.profileImage = profileImage;
        }
        this.updatedAt = ZonedDateTime.now();
    }

    // [두 번째 엔티티] 추가 비즈니스 메서드
    public void updateLastLogin() {
        this.lastLogin = ZonedDateTime.now();
    }

    public void addPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("포인트는 음수일 수 없습니다.");
        }
        this.totalPoints += points;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
