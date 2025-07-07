package com.quizplatform.core.dto.progress;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.Duration;
import java.time.LocalDateTime;

// 개별 참가자의 진행 상황을 나타내는 DTO
@Getter
@Setter
@Builder
public class ParticipantProgress {
    private Long participantId;           // 참가자 ID
    private String username;              // 사용자 이름
    private int currentScore;             // 현재 점수
    private int correctAnswers;           // 맞은 문제 수
    private int totalAnswers;             // 총 답변한 문제 수 (기존 totalAnswered 대신)
    private int totalAnswered;            // 총 답변한 문제 수 (기존 호환성 유지)
    private boolean hasAnsweredCurrent;    // 현재 문제 답변 여부
    private int currentStreak;            // 현재 연속 정답 수
    private double correctRate;           // 정답률
    private Duration averageAnswerTime;    // 평균 답변 시간
    private LocalDateTime lastAnswerTime; // 마지막 답변 시간 (새로 추가)

    // 정답률을 백분율로 계산하는 메서드
    public double getCorrectRate() {
        int total = totalAnswers > 0 ? totalAnswers : totalAnswered;
        if (total == 0) return 0.0;
        return (double) correctAnswers / total * 100;
    }
    
    // 기존 totalAnswered와 새로운 totalAnswers 동기화
    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
        this.totalAnswered = totalAnswers; // 기존 필드와 동기화
    }
}