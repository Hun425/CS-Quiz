package com.quizplatform.quiz.domain.service;

/**
 * 퀴즈 추천 서비스 인터페이스
 * 
 * <p>사용자에게 적합한 퀴즈를 추천하기 위한 서비스입니다.
 * 사용자의 레벨, 선호도, 학습 이력 등을 고려하여 퀴즈를 추천합니다.</p>
 */
public interface QuizRecommendationService {
    
    /**
     * 사용자 추천 설정 초기화
     * 
     * @param userId 사용자 ID
     */
    void initializeRecommendations(String userId);
    
    /**
     * 사용자 레벨에 따른 추천 조정
     * 
     * @param userId 사용자 ID
     * @param level 사용자 레벨
     */
    void adjustRecommendationsByLevel(String userId, int level);
    
    /**
     * 사용자 활동 기반 추천 조정
     * 
     * @param userId 사용자 ID
     * @param categoryId 관심 카테고리 ID
     */
    void adjustRecommendationsByActivity(String userId, String categoryId);
    
    /**
     * 사용자 추천 설정 삭제
     * 
     * @param userId 사용자 ID
     * @return 삭제 성공 여부
     */
    boolean removeRecommendationSettings(String userId);
} 