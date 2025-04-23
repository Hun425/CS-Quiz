package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.event.EventPublisher;
import com.quizplatform.common.event.quiz.QuizCompletedEvent;
import com.quizplatform.common.event.quiz.UserAchievementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 퀴즈 이벤트 발행 서비스
 * 
 * <p>퀴즈 모듈에서 발생하는 이벤트를 카프카 토픽으로 발행합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizEventPublisher {
    
    private final EventPublisher eventPublisher;
    
    // Kafka 토픽 상수
    private static final String QUIZ_COMPLETED_TOPIC = "quiz.completed";
    private static final String USER_ACHIEVEMENT_TOPIC = "user.achievement";
    
    /**
     * 퀴즈 완료 이벤트 발행
     * 
     * @param quizId 퀴즈 ID
     * @param userId 사용자 ID
     * @param score 획득 점수
     * @param totalQuestions 총 문제 수
     * @param experienceGained 획득한 경험치
     * @param pointsGained 획득한 포인트
     * @param passed 통과 여부
     */
    public void publishQuizCompleted(String quizId, String userId, int score, int totalQuestions, 
                                  int experienceGained, int pointsGained, boolean passed) {
        QuizCompletedEvent event = new QuizCompletedEvent(
                quizId, userId, score, totalQuestions, 
                experienceGained, pointsGained, passed
        );
        eventPublisher.publish(event, QUIZ_COMPLETED_TOPIC);
        log.info("Quiz completed event published for quiz: {}, user: {}", quizId, userId);
    }
    
    /**
     * 사용자 업적 이벤트 발행
     * 
     * @param userId 사용자 ID
     * @param achievementType 업적 타입
     * @param achievementDescription 업적 설명
     * @param bonusExperience 보너스 경험치
     * @param bonusPoints 보너스 포인트
     */
    public void publishUserAchievement(String userId, String achievementType, String achievementDescription,
                                   int bonusExperience, int bonusPoints) {
        UserAchievementEvent event = new UserAchievementEvent(
                userId, achievementType, achievementDescription,
                bonusExperience, bonusPoints
        );
        eventPublisher.publish(event, USER_ACHIEVEMENT_TOPIC);
        log.info("User achievement event published for user: {}, achievement: {}", 
                userId, achievementType);
    }
} 