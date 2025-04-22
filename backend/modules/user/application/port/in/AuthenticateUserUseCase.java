package com.quizplatform.user.application.port.in;

import com.quizplatform.user.application.port.in.command.AuthenticateCommand;
import com.quizplatform.user.application.port.in.dto.AuthenticationResult;

/**
 * 사용자 인증 유스케이스 인터페이스 (Command)
 */
public interface AuthenticateUserUseCase {

    /**
     * 제공된 정보로 사용자를 인증합니다.
     *
     * @param command 사용자 인증 정보
     * @return 인증 성공 결과 (사용자 정보, 토큰 등)
     * @throws // 인증 실패 시 예외 발생 (예: BadCredentialsException)
     */
    AuthenticationResult authenticate(AuthenticateCommand command);
} 