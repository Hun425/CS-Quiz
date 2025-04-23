package com.quizplatform.modules.battle.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // For potential questionResults in ParticipantResult

// 배틀 최종 결과 응답 DTO (기존 BattleResult 리팩토링)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResultResponse {
    private Long roomId;
    private ParticipantResult winner; // 내부 DTO 타입 사용
    private List<ParticipantResult> participants; // 내부 DTO 타입 사용
    private int highestScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalTimeSeconds; // 서비스에서 계산된 값
    private int totalQuestions; // 서비스에서 계산된 값

    // 내부 클래스로 참가자 결과 정의 (BattleEndResponse와 유사하지만 필요한 필드만 포함)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantResult {
        private Long userId;
        private int finalScore;
        private int correctAnswers;
        private int averageTimeSeconds; // 필요시 추가
        private boolean isWinner;
        // 필요하다면 문제별 정답 여부 포함: Map<Long, Boolean> questionResults;
        // 예시에서는 주석 처리. 실제 필요 여부에 따라 활성화.
    }
} 