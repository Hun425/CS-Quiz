package com.quizplatform.battle.application.port.in.command;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 배틀 생성 커맨드
 */
@Getter
@Builder
public class CreateBattleCommand {
    private final UUID challengerId;
    private final UUID opponentId;
    private final UUID quizId;
    private final int timeLimit;
}
