package com.quizplatform.battle.application.port.in;

import com.quizplatform.battle.application.port.in.command.SubmitBattleAnswerCommand;
import com.quizplatform.battle.domain.model.BattleParticipant;

/**
 * 배틀 답변 제출 유스케이스
 */
public interface SubmitBattleAnswerUseCase {
    /**
     * 배틀에서 답변 제출
     * 
     * @param command 답변 제출 커맨드
     * @return 갱신된 배틀 참가자 정보
     */
    BattleParticipant submitAnswer(SubmitBattleAnswerCommand command);
}
