package com.quizplatform.battle.application.port.in.command;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * 배틀 시작 커맨드
 */
@Getter
@Builder
public class StartBattleCommand {
    private final UUID battleId;
    private final UUID userId;
}
