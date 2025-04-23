package com.quizplatform.common.event.quiz;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 퀴즈 점수 계산 요청 이벤트
 */
@Getter
@ToString
@AllArgsConstructor
public class QuizScoreCalculationRequestEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String requestId;
    private final String userId;
    private final String questionId;
    private final boolean isCorrect;
    private final long responseTimeMs;

    public QuizScoreCalculationRequestEvent(String requestId, String userId, String questionId, 
                                          boolean isCorrect, long responseTimeMs) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.requestId = requestId;
        this.userId = userId;
        this.questionId = questionId;
        this.isCorrect = isCorrect;
        this.responseTimeMs = responseTimeMs;
    }
}
