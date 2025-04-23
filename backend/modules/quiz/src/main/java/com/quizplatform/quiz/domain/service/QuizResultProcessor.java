package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.domain.model.QuizAttempt;

/**
 * 퀴즈 결과 처리 서비스 인터페이스
 * 
 * <p>퀴즈 시도 결과를 처리하고 통계, 점수, 보상 등을 계산합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface QuizResultProcessor {
    
    /**
     * 퀴즈 시도 완료 처리
     * 
     * @param attempt 완료된 퀴즈 시도
     * @return 처리된 퀴즈 시도
     */
    QuizAttempt processQuizCompletion(QuizAttempt attempt);
    
    /**
     * 퀴즈 결과 이벤트 발행
     * 
     * @param attempt 완료된 퀴즈 시도
     */
    void publishQuizCompletionEvent(QuizAttempt attempt);
    
    /**
     * 사용자 업적 확인 및 이벤트 발행
     * 
     * @param attempt 완료된 퀴즈 시도
     */
    void checkAndPublishAchievements(QuizAttempt attempt);
    
    /**
     * 퀴즈 통계 업데이트
     * 
     * @param attempt 완료된 퀴즈 시도
     */
    void updateQuizStatistics(QuizAttempt attempt);
    
    /**
     * 획득 경험치 계산
     * 
     * @param attempt 완료된 퀴즈 시도
     * @return 획득 경험치
     */
    int calculateExperienceGained(QuizAttempt attempt);
    
    /**
     * 획득 포인트 계산
     * 
     * @param attempt 완료된 퀴즈 시도
     * @return 획득 포인트
     */
    int calculatePointsGained(QuizAttempt attempt);
} 