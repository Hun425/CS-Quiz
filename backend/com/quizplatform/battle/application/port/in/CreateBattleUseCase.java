package com.quizplatform.battle.application.port.in;

import com.quizplatform.battle.application.port.in.command.CreateBattleCommand;
import com.quizplatform.battle.domain.model.Battle;

import java.util.UUID;

/**
 * 배틀 생성 유스케이스
 */
public interface CreateBattleUseCase {
    /**
     * 새 배틀을 생성
     * 
     * @param command 배틀 생성 커맨드
     * @return 생성된 배틀 ID
     */
    UUID createBattle(CreateBattleCommand command);
}
