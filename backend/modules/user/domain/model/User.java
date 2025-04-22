package com.quizplatform.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Objects; // For equals check in setters
import java.util.Set; // For roles

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
    private String nickname; // Added nickname field
    private String password; // Added password field
    private String profileImage;
    private Set<UserRole> roles; // Changed to Set<UserRole>
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
    public User(Long id, AuthProvider provider, String providerId, String email, String username, String password, String nickname, String profileImage, Set<UserRole> roles, boolean isActive, int totalPoints, int level, int experience, int requiredExperience, String accessToken, String refreshToken, ZonedDateTime tokenExpiresAt, ZonedDateTime lastLogin, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.username = username;
        this.password = password; // Added password to builder
        this.nickname = (nickname == null || nickname.isBlank()) ? username : nickname; // Default nickname to username if blank
        this.profileImage = profileImage;
        this.roles = (roles == null || roles.isEmpty()) ? Set.of(UserRole.ROLE_USER) : roles; // Default role
        this.isActive = isActive;
        this.totalPoints = totalPoints;
        this.level = (level <= 0) ? 1 : level; // Default level
        this.experience = experience;
        this.requiredExperience = (requiredExperience <= 0) ? calculateNextLevelExp(this.level) : requiredExperience; // Default required exp based on level
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- 비즈니스 로직 메소드 ---

    /**
     * 경험치 획득 및 레벨업 처리
     *
     * @param exp 획득한 경험치
     * @return 레벨업 발생 여부
     */
    public boolean gainExperience(int exp) {
        if (exp <= 0) return false; // 유효하지 않은 경험치
        this.experience += exp;
        this.totalPoints += exp;
        boolean leveledUp = false;
        while (this.experience >= this.requiredExperience) {
            levelUp();
            leveledUp = true;
        }
        this.markAsUpdated();
        return leveledUp;
    }

    /**
     * 레벨업 처리 (내부 호출용)
     */
    private void levelUp() {
        this.level++;
        this.experience -= this.requiredExperience;
        if (this.experience < 0) this.experience = 0; // 경험치가 음수가 되지 않도록
        this.requiredExperience = calculateNextLevelExp(this.level);
        // TODO: 레벨업 이벤트 발행 (필요시 DomainEventPublisher 사용)
    }

    /**
     * 특정 레벨에 필요한 경험치 계산 (static or instance method)
     *
     * @param targetLevel 계산할 레벨
     * @return 필요 경험치
     */
    private int calculateNextLevelExp(int targetLevel) {
        // 예시: 레벨 * 100 (실제 로직으로 대체 필요)
        if (targetLevel <= 0) return 100; // 최소값
        return targetLevel * 100 + (int) (Math.pow(targetLevel, 1.5) * 10); // 좀 더 복잡한 예시
    }

    /**
     * 레벨과 경험치를 직접 업데이트 (주의해서 사용해야 함, 보통 gainExperience 사용 권장)
     * @param newLevel 새 레벨
     * @param newExperience 새 경험치
     */
    public void updateLevelAndExp(int newLevel, int newExperience) {
       if (newLevel > 0 && newLevel != this.level) {
            this.level = newLevel;
            this.requiredExperience = calculateNextLevelExp(this.level);
        }
        if (newExperience >= 0 && newExperience < this.requiredExperience) {
            this.experience = newExperience;
        } else if (newExperience >= this.requiredExperience) {
           // 경험치가 요구 경험치를 넘으면 레벨업 로직을 타는게 맞지만, 여기서는 일단 상한선까지만 설정
           this.experience = this.requiredExperience -1;
        } else {
           this.experience = 0; // 음수 방지
        }
        this.markAsUpdated();
    }


    /**
     * 닉네임 업데이트
     * @param newNickname 새 닉네임
     */
     public void updateNickname(String newNickname) {
         if (newNickname != null && !newNickname.isBlank() && !Objects.equals(this.nickname, newNickname)) {
             this.nickname = newNickname.trim();
             this.markAsUpdated();
         }
     }

    /**
     * 이메일 업데이트
     * @param newEmail 새 이메일
     */
     public void updateEmail(String newEmail) {
         // 이메일 유효성 검사는 Application Service 에서 처리하는 것이 일반적
         if (newEmail != null && !newEmail.isBlank() && !Objects.equals(this.email, newEmail)) {
             this.email = newEmail.trim();
             this.markAsUpdated();
         }
     }


    /**
     * 프로필 정보 업데이트 (예시: username, profileImage)
     * @param newUsername 새 사용자명 (null 이 아니면 업데이트)
     * @param newProfileImage 새 프로필 이미지 URL (null 허용 가능)
     */
    public void updateProfile(String newUsername, String newProfileImage) {
        boolean changed = false;
        if (newUsername != null && !newUsername.trim().isEmpty() && !Objects.equals(this.username, newUsername)) {
            this.username = newUsername.trim();
            changed = true;
        }
        if (!Objects.equals(this.profileImage, newProfileImage)) {
           this.profileImage = newProfileImage;
           changed = true;
        }

        if(changed) this.markAsUpdated();
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLogin = ZonedDateTime.now();
        this.markAsUpdated();
    }

     /**
     * 계정 비활성화
     */
    public void deactivate() {
        if(this.isActive) {
           this.isActive = false;
           this.markAsUpdated();
            // TODO: UserDeactivated 이벤트 발행 (필요시)
        }
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        if(!this.isActive) {
           this.isActive = true;
           this.markAsUpdated();
           // TODO: UserActivated 이벤트 발행 (필요시)
        }
    }

    // 업데이트 시간 갱신 헬퍼
    private void markAsUpdated() {
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * 포인트를 추가합니다.
     * @param points 추가할 포인트 (양수)
     */
    public void addPoints(int points) {
        if (points > 0) {
            this.totalPoints += points;
            this.markAsUpdated();
        }
    }
    
    /**
     * 역할(roles)을 업데이트합니다.
     * @param roles 새 역할 집합
     */
    public void updateRoles(Set<UserRole> roles) {
        if (roles != null && !roles.isEmpty()) {
            this.roles = roles;
            this.markAsUpdated();
        }
    }
    
    /**
     * 인증 제공자 정보를 업데이트합니다.
     * @param provider 새 인증 제공자
     * @param providerId 새 제공자 ID
     */
    public void updateProvider(AuthProvider provider, String providerId) {
        if (provider != null && providerId != null && !providerId.isBlank()) {
            this.provider = provider;
            this.providerId = providerId;
            this.markAsUpdated();
        }
    }
    
    // TODO: 그 외 필요한 비즈니스 로직 추가
} 