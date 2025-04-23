package com.quizplatform.quiz.domain.event;

import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizAttempt;
import lombok.Getter;

import java.util.UUID;

/**
 * 퀴즈 완료 이벤트
 * 
 * <p>사용자가 퀴즈를 완료했을 때 발생하는 이벤트입니다.
 * 사용자 모듈에서 이 이벤트를 수신하여 경험치와 포인트를 부여합니다.</p>
 */
@Getter
public class QuizCompletedEvent implements QuizEvent {
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
     * @param quizAttempt 퀴즈 시도 정보
     * @param quiz 퀴즈 정보
     * @param experienceGained 획득한 경험치
     * @param pointsGained 획득한 포인트
     */
    public QuizCompletedEvent(QuizAttempt quizAttempt, Quiz quiz, int experienceGained, int pointsGained) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.quizId = quiz.getId().toString();
        this.userId = quizAttempt.getUser().getId().toString();
        this.score = quizAttempt.getScore();
        this.totalQuestions = quizAttempt.getTotalQuestions();
        this.experienceGained = experienceGained;
        this.pointsGained = pointsGained;
        this.passed = quizAttempt.isPassed();
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "QUIZ_COMPLETED";
    }
} 