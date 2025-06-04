package com.quizplatform.user.application.service;

import com.quizplatform.user.domain.model.AuthProvider;
import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserRole;
import com.quizplatform.user.adapter.out.persistence.repository.UserRepository;
import com.quizplatform.user.adapter.out.event.UserEventPublisher;
import com.quizplatform.user.application.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("사용자 ID로 조회 테스트")
    void findByIdTest() {
        // given
        Long userId = 1L;
        User mockUser = createUserWithId(userId, "test@example.com", "testuser");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        
        // when
        Optional<User> foundUser = userService.findById(userId);
        
        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        
        verify(userRepository, times(1)).findById(userId);
    }
    
    @Test
    @DisplayName("사용자명으로 조회 테스트")
    void findByUsernameTest() {
        // given
        String username = "testuser";
        User mockUser = createUserWithId(1L, "test@example.com", username);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        
        // when
        Optional<User> foundUser = userService.findByUsername(username);
        
        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(username);
        
        verify(userRepository, times(1)).findByUsername(username);
    }
    
    @Test
    @DisplayName("사용자 생성 테스트")
    void createUserTest() {
        // given
        User newUser = User.builder()
                .email("new@example.com")
                .username("newuser")
                .profileImage("http://example.com/new-profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("12345")
                .build();
        
        User savedUser = createUserWithId(1L, "new@example.com", "newuser");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(eventPublisher).publishUserCreated(any(User.class));
        
        // when
        User createdUser = userService.createUser(newUser);
        
        // then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(1L);
        assertThat(createdUser.getEmail()).isEqualTo("new@example.com");
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, times(1)).publishUserCreated(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 프로필 업데이트 테스트")
    void updateProfileTest() {
        // given
        Long userId = 1L;
        User existingUser = createUserWithId(userId, "test@example.com", "oldUsername");
        User updatedUser = createUserWithId(userId, "test@example.com", "newUsername");
        updatedUser.updateProfile("newUsername", "http://example.com/updated-profile.jpg");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        // when
        User result = userService.updateProfile(userId, "newUsername", "http://example.com/updated-profile.jpg");
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newUsername");
        assertThat(result.getProfileImage()).isEqualTo("http://example.com/updated-profile.jpg");
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 경험치 부여 및 레벨업 테스트")
    void giveExperienceTest() {
        // given
        Long userId = 1L;
        User user = createUserWithId(userId, "test@example.com", "testuser");
        
        // 레벨업 조건 충족
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(eventPublisher).publishUserLevelUp(any(User.class), anyInt());
        
        // when
        boolean leveledUp = userService.giveExperience(userId, 120);
        
        // then
        assertThat(leveledUp).isTrue();
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, times(1)).publishUserLevelUp(any(User.class), anyInt());
    }
    
    @Test
    @DisplayName("사용자 목록 조회 테스트")
    void findAllUsersTest() {
        // given
        List<User> mockUsers = Arrays.asList(
            createUserWithId(1L, "user1@example.com", "user1"),
            createUserWithId(2L, "user2@example.com", "user2")
        );
        
        when(userRepository.findAll()).thenReturn(mockUsers);
        
        // when
        List<User> users = userService.findAllUsers();
        
        // then
        assertThat(users).isNotNull();
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUsername()).isEqualTo("user1");
        assertThat(users.get(1).getUsername()).isEqualTo("user2");
        
        verify(userRepository, times(1)).findAll();
    }
    
    // 헬퍼 메서드: ID가 설정된 유저 생성
    private User createUserWithId(Long id, String email, String username) {
        User user = User.builder()
                .email(email)
                .username(username)
                .profileImage("http://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("12345")
                .build();
                
        // ID 필드 리플렉션으로 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return user;
    }
} 