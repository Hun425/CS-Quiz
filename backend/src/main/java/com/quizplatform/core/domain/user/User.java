package com.quizplatform.core.domain.user;



import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
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

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "last_login")
    private ZonedDateTime lastLogin;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        if (role == null) {
            role = UserRole.USER;
        }
        if (totalPoints == null) {
            totalPoints = 0;
        }
        isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

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
    }

    // 비즈니스 메서드
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