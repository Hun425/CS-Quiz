package com.quizplatform.user.application.port.in;

import com.quizplatform.user.application.port.in.command.UpdateUserLevelCommand;

/**
 * 사용자 레벨 업데이트 유스케이스 인터페이스 (Command)
 */
public interface UpdateUserLevelUseCase {

    /**
     * 사용자의 레벨을 업데이트합니다.
     * 레벨 변경 이력도 함께 기록될 수 있습니다.
     *
     * @param command 레벨 업데이트 정보
     * @throws // 사용자 조회 실패, 유효하지 않은 레벨 등의 예외 발생 가능
     */
    void updateUserLevel(UpdateUserLevelCommand command);
} 