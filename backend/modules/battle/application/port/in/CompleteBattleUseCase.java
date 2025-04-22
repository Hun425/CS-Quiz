package com.quizplatform.battle.application.port.in;

import com.quizplatform.battle.application.port.in.command.CompleteBattleCommand;
import com.quizplatform.battle.domain.model.BattleSummary;

/**
 * 배틀 완료 유스케이스
 */
public interface CompleteBattleUseCase {
    /**
     * 배틀 완료
     * 
     * @param command 배틀 완료 커맨드
     * @return 배틀 요약 정보
     */
    BattleSummary completeBattle(CompleteBattleCommand command);
}
