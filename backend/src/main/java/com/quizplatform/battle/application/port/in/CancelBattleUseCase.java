package com.quizplatform.battle.application.port.in;

import com.quizplatform.battle.application.port.in.command.CancelBattleCommand;
import com.quizplatform.battle.domain.model.Battle;

/**
 * 배틀 취소 유스케이스
 */
public interface CancelBattleUseCase {
    /**
     * 배틀 취소
     * 
     * @param command 배틀 취소 커맨드
     * @return 취소된 배틀 객체
     */
    Battle cancelBattle(CancelBattleCommand command);
}
