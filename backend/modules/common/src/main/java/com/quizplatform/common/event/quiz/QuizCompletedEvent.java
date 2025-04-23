package com.quizplatform.common.event.quiz;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 퀴즈 완료 이벤트
 * 
 * <p>사용자가 퀴즈를 완료했을 때 발생하는 이벤트입니다.
 * 퀴즈 모듈에서 발행하고, 사용자 모듈에서 수신하여 경험치와 포인트를 부여합니다.</p>
 */
@Getter
@ToString
@AllArgsConstructor
public class QuizCompletedEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String quizId;
    private final String userId;
    private final int score;
    private final int totalQuestions;
    private final int experienceGained;
    private final int pointsGained;
    private final boolean passed;
    
    /**
     * 퀴즈 완료 이벤트 생성자
     * 
     * @param quizId 퀴즈 ID
     * @param userId 사용자 ID
     * @param score 획득 점수
     * @param totalQuestions 총 문제 수
     * @param experienceGained 획득한 경험치
     * @param pointsGained 획득한 포인트
     * @param passed 통과 여부
     */
    public QuizCompletedEvent(String quizId, String userId, int score, int totalQuestions, 
                              int experienceGained, int pointsGained, boolean passed) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.experienceGained = experienceGained;
        this.pointsGained = pointsGained;
        this.passed = passed;
    }
} 