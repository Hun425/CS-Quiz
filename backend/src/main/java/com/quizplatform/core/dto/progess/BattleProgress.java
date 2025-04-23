package com.quizplatform.core.dto.progess;

import com.quizplatform.modules.battle.domain.entity.BattleRoomStatus;
import lombok.Getter;
import lombok.*;

import java.util.Map;
import java.time.Duration;
import java.util.Comparator;

// 배틀 진행 상황을 나타내는 DTO
@Getter
@Builder
public class BattleProgress {
    private Long battleRoomId;                    // 배틀룸 ID
    private int currentQuestionIndex;             // 현재 문제 인덱스
    private int totalQuestions;                   // 전체 문제 수
    private Duration remainingTime;               // 현재 문제의 남은 시간
    private Map<Long, ParticipantProgress> participantProgresses; // 참가자별 진행 상황
    private BattleRoomStatus status;             // 배틀룸 상태
    private int currentQuestionPoints;            // 현재 문제의 기본 점수
    private boolean allParticipantsAnswered;      // 모든 참가자가 답변했는지 여부
    private Duration averageAnswerTime;           // 현재까지의 평균 답변 시간

    // 특정 참가자의 진행 상황을 쉽게 조회하기 위한 메서드
    public ParticipantProgress getParticipantProgress(Long participantId) {
        return participantProgresses.getOrDefault(participantId, null);
    }

    // 현재 1등인 참가자의 정보를 반환하는 메서드
    public ParticipantProgress getLeader() {
        return participantProgresses.values().stream()
                .max(Comparator.comparingInt(ParticipantProgress::getCurrentScore))
                .orElse(null);
    }

    // 진행률을 백분율로 반환하는 메서드
    public double getProgressPercentage() {
        return (double) currentQuestionIndex / totalQuestions * 100;
    }
}
