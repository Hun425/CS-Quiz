package com.quizplatform.user.infrastructure.http;

import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserRole;
import com.quizplatform.user.domain.service.UserService;
import com.quizplatform.user.infrastructure.http.dto.UserCreationRequest;
import com.quizplatform.user.infrastructure.http.dto.UserProfileUpdateRequest;
import com.quizplatform.user.infrastructure.http.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 모든 사용자 조회
     * 
     * @return 사용자 목록
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * ID로 사용자 조회
     * 
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 생성
     * 
     * @param request 사용자 생성 요청
     * @return 생성된 사용자 정보
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreationRequest request) {
        User user = request.toEntity();
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.fromEntity(createdUser));
    }

    /**
     * 사용자 프로필 업데이트
     * 
     * @param id 사용자 ID
     * @param request 프로필 업데이트 요청
     * @return 업데이트된 사용자 정보
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UserProfileUpdateRequest request) {
        User updatedUser = userService.updateProfile(id, request.getUsername(), request.getProfileImage());
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    /**
     * 사용자 활성화/비활성화 토글
     * 
     * @param id 사용자 ID
     * @return 업데이트된 사용자 정보
     */
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleActive(@PathVariable Long id) {
        User updatedUser = userService.toggleActive(id);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    /**
     * 사용자 권한 변경
     * 
     * @param id 사용자 ID
     * @param role 새 권한
     * @return 업데이트된 사용자 정보
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @RequestParam UserRole role) {
        User updatedUser = userService.updateRole(id, role);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    /**
     * 사용자에게 경험치 부여
     * 
     * @param id 사용자 ID
     * @param experience 경험치
     * @return 레벨업 여부
     */
    @PostMapping("/{id}/experience")
    public ResponseEntity<Boolean> giveExperience(
            @PathVariable Long id,
            @RequestParam int experience) {
        boolean leveledUp = userService.giveExperience(id, experience);
        return ResponseEntity.ok(leveledUp);
    }

    /**
     * 사용자에게 포인트 부여
     * 
     * @param id 사용자 ID
     * @param points 포인트
     * @return 성공 응답
     */
    @PostMapping("/{id}/points")
    public ResponseEntity<Void> givePoints(
            @PathVariable Long id,
            @RequestParam int points) {
        userService.givePoints(id, points);
        return ResponseEntity.ok().build();
    }
} 