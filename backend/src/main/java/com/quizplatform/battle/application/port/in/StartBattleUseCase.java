package com.quizplatform.battle.application.port.in;

import com.quizplatform.battle.application.port.in.command.StartBattleCommand;
import com.quizplatform.battle.domain.model.Battle;

/**
 * 배틀 시작 유스케이스
 */
public interface StartBattleUseCase {
    /**
     * 배틀 시작
     * 
     * @param command 배틀 시작 커맨드
     * @return 시작된 배틀 객체
     */
    Battle startBattle(StartBattleCommand command);
}
