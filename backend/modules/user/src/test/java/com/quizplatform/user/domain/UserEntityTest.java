package com.quizplatform.user.domain;

import com.quizplatform.user.domain.model.AuthProvider;
import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("사용자 엔티티 생성 테스트")
    void createUserTest() {
        // given
        String email = "test@example.com";
        String username = "testuser";
        String profileImage = "http://example.com/profile.jpg";
        String providerId = "12345";
        
        // when
        User user = User.builder()
                .email(email)
                .username(username)
                .profileImage(profileImage)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .build();
        
        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getProfileImage()).isEqualTo(profileImage);
        assertThat(user.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(user.getProviderId()).isEqualTo(providerId);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getLevel()).isEqualTo(1);
        assertThat(user.getExperience()).isEqualTo(0);
        assertThat(user.getTotalPoints()).isEqualTo(0);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getBattleStats()).isNotNull();
    }
    
    @Test
    @DisplayName("사용자 프로필 업데이트 테스트")
    void updateUserProfileTest() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .profileImage("http://example.com/old-profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("12345")
                .build();
        
        String newUsername = "newUsername";
        String newProfileImage = "http://example.com/new-profile.jpg";
        
        // when
        user.updateProfile(newUsername, newProfileImage);
        
        // then
        assertThat(user.getUsername()).isEqualTo(newUsername);
        assertThat(user.getProfileImage()).isEqualTo(newProfileImage);
    }
    
    @Test
    @DisplayName("사용자 경험치 획득 및 레벨업 테스트")
    void gainExperienceTest() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .profileImage("http://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("12345")
                .build();
        
        int initialLevel = user.getLevel();
        int initialExp = user.getExperience();
        int initialPoints = user.getTotalPoints();
        
        // when: 레벨업에 충분한 경험치 획득
        boolean leveledUp = user.gainExperience(120);
        
        // then
        assertThat(leveledUp).isTrue();
        assertThat(user.getLevel()).isGreaterThan(initialLevel);
        assertThat(user.getExperience()).isGreaterThan(initialExp);
        assertThat(user.getTotalPoints()).isEqualTo(initialPoints + 120);
    }
    
    @Test
    @DisplayName("사용자 권한 변경 테스트")
    void updateRoleTest() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .profileImage("http://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("12345")
                .build();
        
        // when
        user.updateRole(UserRole.ADMIN);
        
        // then
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }
    
    @Test
    @DisplayName("사용자 계정 활성화/비활성화 테스트")
    void toggleActiveTest() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .profileImage("http://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("12345")
                .build();
        
        boolean initialState = user.isActive();
        
        // when
        user.toggleActive();
        
        // then
        assertThat(user.isActive()).isEqualTo(!initialState);
    }
} 