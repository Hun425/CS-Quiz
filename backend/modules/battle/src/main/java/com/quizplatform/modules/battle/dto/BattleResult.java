package com.quizplatform.modules.battle.dto;

import com.quizplatform.modules.battle.domain.BattleParticipant;
import com.quizplatform.modules.battle.domain.BattleRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BattleResult {
    private final Long roomId;
    private final BattleParticipant winner;
    private final List<BattleParticipant> participants;
    private final int highestScore;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int totalTimeSeconds;
    private final int totalQuestions;
    private final BattleRoom battleRoom;  // 배틀룸 참조 추가

    public int getTotalQuestions() {
        return battleRoom.getQuestions().size();
    }

    public int getTotalTimeSeconds() {
        return (int) java.time.Duration.between(startTime, endTime).getSeconds();
    }
}