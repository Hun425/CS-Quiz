package com.quizplatform.user.application.port.in;

import com.quizplatform.user.application.port.in.command.UpdateUserProfileCommand;
import com.quizplatform.user.domain.model.User;

/**
 * 사용자 프로필 업데이트 유스케이스 인터페이스 (Command)
 */
public interface UpdateUserProfileUseCase {

    /**
     * 사용자의 프로필 정보를 업데이트합니다.
     *
     * @param command 프로필 업데이트 정보 (userId 필수, 변경할 필드만 값 포함)
     * @return 업데이트된 User 도메인 모델
     * @throws // 사용자 조회 실패 등의 예외 발생 가능
     */
    User updateUserProfile(UpdateUserProfileCommand command);
} 