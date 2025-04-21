package com.quizplatform.modules.user.api;

import com.quizplatform.modules.user.dto.AuthResponse;
import com.quizplatform.modules.user.dto.UserProfileDto;
import com.quizplatform.modules.user.dto.UserProfileUpdateRequest;
import com.quizplatform.modules.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 모듈 API 인터페이스
 * <p>
 * 사용자 모듈이 다른 모듈에 제공하는 API를 정의합니다.
 * </p>
 */
@RequestMapping("/api/v1/users")
public interface UserModuleApi {

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUserById(@PathVariable Long userId);

    /**
     * 현재 인증된 사용자의 프로필을 조회합니다.
     *
     * @return 사용자 프로필
     */
    @GetMapping("/me")
    ResponseEntity<UserProfileDto> getCurrentUserProfile();

    /**
     * 사용자 프로필을 업데이트합니다.
     *
     * @param request 프로필 업데이트 요청
     * @return 업데이트된 사용자 프로필
     */
    @PutMapping("/me")
    ResponseEntity<UserProfileDto> updateUserProfile(@RequestBody UserProfileUpdateRequest request);

    /**
     * 사용자 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효한 경우 사용자 정보를 포함한 응답
     */
    @PostMapping("/validate-token")
    ResponseEntity<AuthResponse> validateToken(@RequestParam String token);
}