package com.quizplatform.modules.user.service;

import com.quizplatform.modules.user.dto.UserProfileDto;
import com.quizplatform.modules.user.dto.UserProfileUpdateRequest;
import com.quizplatform.modules.user.dto.UserResponse;

/**
 * 사용자 서비스 인터페이스
 * <p>
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * </p>
 */
public interface UserService {

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    UserResponse getUserById(Long userId);

    /**
     * 사용자 ID로 사용자 프로필을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필
     */
    UserProfileDto getUserProfile(Long userId);

    /**
     * 사용자 프로필을 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @param request 프로필 업데이트 요청
     * @return 업데이트된 사용자 프로필
     */
    UserProfileDto updateUserProfile(Long userId, UserProfileUpdateRequest request);
}