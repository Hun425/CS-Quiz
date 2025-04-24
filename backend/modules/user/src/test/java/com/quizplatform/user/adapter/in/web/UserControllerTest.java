package com.quizplatform.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizplatform.user.domain.model.AuthProvider;
import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserRole;
import com.quizplatform.user.domain.service.UserService;
import com.quizplatform.user.infrastructure.http.UserController;
import com.quizplatform.user.infrastructure.http.dto.UserCreationRequest;
import com.quizplatform.user.infrastructure.http.dto.UserProfileUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @DisplayName("모든 사용자 조회 테스트")
    void getAllUsersTest() throws Exception {
        // given
        User user1 = createUserWithId(1L, "test1@example.com", "user1");
        User user2 = createUserWithId(2L, "test2@example.com", "user2");
        List<User> users = Arrays.asList(user1, user2);

        when(userService.findAllUsers()).thenReturn(users);

        // when & then
        mockMvc.perform(get("/api/users"))
                .andDo(print())  // 결과 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("user2")));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @DisplayName("ID로 사용자 조회 테스트 - 성공")
    void getUserByIdTest_Success() throws Exception {
        // given
        Long userId = 1L;
        User user = createUserWithId(userId, "test@example.com", "testuser");

        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andDo(print())  // 결과 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).findById(userId);
    }

    @Test
    @DisplayName("ID로 사용자 조회 테스트 - 실패(사용자 없음)")
    void getUserByIdTest_NotFound() throws Exception {
        // given
        Long userId = 999L;

        when(userService.findById(userId)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andDo(print())  // 결과 출력
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(userId);
    }

    @Test
    @DisplayName("사용자 생성 테스트")
    void createUserTest() throws Exception {
        // given
        UserCreationRequest request = UserCreationRequest.builder()
                .provider("GOOGLE")
                .providerId("12345")
                .email("new@example.com")
                .username("newuser")
                .profileImage("http://example.com/new-profile.jpg")
                .build();

        User createdUser = createUserWithId(1L, "new@example.com", "newuser");

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // 결과 출력
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("new@example.com")));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("사용자 프로필 업데이트 테스트")
    void updateProfileTest() throws Exception {
        // given
        Long userId = 1L;
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .username("updatedUser")
                .profileImage("http://example.com/updated-profile.jpg")
                .build();

        User updatedUser = createUserWithId(userId, "test@example.com", "updatedUser");
        updatedUser.updateProfile("updatedUser", "http://example.com/updated-profile.jpg");

        when(userService.updateProfile(eq(userId), anyString(), anyString())).thenReturn(updatedUser);

        // when & then
        mockMvc.perform(put("/api/users/{id}/profile", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // 결과 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("updatedUser")))
                .andExpect(jsonPath("$.profileImage", is("http://example.com/updated-profile.jpg")));

        verify(userService, times(1)).updateProfile(eq(userId), eq("updatedUser"), eq("http://example.com/updated-profile.jpg"));
    }

    @Test
    @DisplayName("사용자 활성화 상태 토글 테스트")
    void toggleActiveTest() throws Exception {
        // given
        Long userId = 1L;
        User user = createUserWithId(userId, "test@example.com", "testuser");
        user.toggleActive(); // 비활성화 상태로 변경

        when(userService.toggleActive(userId)).thenReturn(user);

        // when & then
        mockMvc.perform(put("/api/users/{id}/toggle-active", userId))
                .andDo(print())  // 결과 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.active", is(false)));

        verify(userService, times(1)).toggleActive(userId);
    }

    @Test
    @DisplayName("사용자 권한 변경 테스트")
    void updateRoleTest() throws Exception {
        // given
        Long userId = 1L;
        UserRole newRole = UserRole.ADMIN;

        User updatedUser = createUserWithId(userId, "test@example.com", "testuser");
        updatedUser.updateRole(newRole);

        when(userService.updateRole(userId, newRole)).thenReturn(updatedUser);

        // when & then
        mockMvc.perform(put("/api/users/{id}/role", userId)
                        .param("role", newRole.name()))
                .andDo(print())  // 결과 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.role", is(newRole.name())));

        verify(userService, times(1)).updateRole(userId, newRole);
    }

    @Test
    @DisplayName("사용자 경험치 부여 테스트")
    void giveExperienceTest() throws Exception {
        // given
        Long userId = 1L;
        int experience = 100;

        when(userService.giveExperience(userId, experience)).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/users/{id}/experience", userId)
                        .param("experience", String.valueOf(experience)))
                .andDo(print())  // 결과 출력
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).giveExperience(userId, experience);
    }

    @Test
    @DisplayName("사용자 포인트 부여 테스트")
    void givePointsTest() throws Exception {
        // given
        Long userId = 1L;
        int points = 50;

        doNothing().when(userService).givePoints(userId, points);

        // when & then
        mockMvc.perform(post("/api/users/{id}/points", userId)
                        .param("points", String.valueOf(points)))
                .andDo(print())  // 결과 출력
                .andExpect(status().isOk());

        verify(userService, times(1)).givePoints(userId, points);
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