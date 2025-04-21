package com.quizplatform.modules.battle.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class ParticipantProgress {
    private Long participantId;           // 참가자 ID
    private String username;              // 사용자 이름
    private int currentScore;             // 현재 점수
    private int correctAnswers;           // 맞은 문제 수
    private int totalAnswered;            // 총 답변한 문제 수
    private boolean hasAnsweredCurrent;    // 현재 문제 답변 여부
    private int currentStreak;            // 현재 연속 정답 수
    private double correctRate;           // 정답률
    private Duration averageAnswerTime;    // 평균 답변 시간

    // 정답률을 백분율로 계산하는 메서드
    public double getCorrectRate() {
        if (totalAnswered == 0) return 0.0;
        return (double) correctAnswers / totalAnswered * 100;
    }
}