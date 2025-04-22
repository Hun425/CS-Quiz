package com.quizplatform.battle.application.port.in.command;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 배틀 답변 제출 커맨드
 */
@Getter
@Builder
public class SubmitBattleAnswerCommand {
    private final UUID battleId;
    private final UUID userId;
    private final UUID questionId;
    private final UUID selectedOptionId;
    private final int timeSpentInSeconds;
}
