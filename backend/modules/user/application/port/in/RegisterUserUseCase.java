package com.quizplatform.user.application.port.in;

import com.quizplatform.user.application.port.in.command.RegisterUserCommand;
import com.quizplatform.user.domain.model.User;

/**
 * 사용자 등록 유스케이스 인터페이스 (Command)
 */
public interface RegisterUserUseCase {

    /**
     * 제공된 정보로 새로운 사용자를 등록합니다.
     *
     * @param command 사용자 등록 정보
     * @return 등록된 User 도메인 모델
     * @throws // 사용자 이름 중복 등 예외 발생 가능
     */
    User registerUser(RegisterUserCommand command);
} 