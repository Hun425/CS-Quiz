package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizAttempt;

import java.util.List;
import java.util.Optional;

/**
 * 퀴즈 서비스 인터페이스
 * 
 * <p>퀴즈 도메인의 핵심 비즈니스 로직을 정의한 인터페이스입니다.
 * 퀴즈 관리, 추천, 시도 등의 기능을 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface QuizService {
    
    /**
     * 퀴즈 생성
     * 
     * @param quiz 생성할 퀴즈 정보
     * @return 생성된 퀴즈
     */
    Quiz createQuiz(Quiz quiz);
    
    /**
     * ID로 퀴즈 조회
     * 
     * @param id 퀴즈 ID
     * @return 퀴즈 Optional
     */
    Optional<Quiz> findById(Long id);
    
    /**
     * 카테고리로 퀴즈 목록 조회
     * 
     * @param category 카테고리
     * @return 퀴즈 목록
     */
    List<Quiz> findByCategory(String category);
    
    /**
     * 난이도로 퀴즈 목록 조회
     * 
     * @param difficulty 난이도
     * @return 퀴즈 목록
     */
    List<Quiz> findByDifficulty(int difficulty);
    
    /**
     * 퀴즈 시도 생성
     * 
     * @param quizId 퀴즈 ID
     * @param userId 사용자 ID
     * @return 생성된 퀴즈 시도
     */
    QuizAttempt startQuizAttempt(Long quizId, Long userId);
    
    /**
     * 퀴즈 시도 제출 및 평가
     * 
     * @param attemptId 퀴즈 시도 ID
     * @return 평가 결과
     */
    QuizAttempt submitQuizAttempt(Long attemptId);
    
    /**
     * 새 사용자 정보 처리
     * 
     * <p>다른 모듈에서 사용자 등록 이벤트를 수신했을 때 호출됩니다.</p>
     * 
     * @param userId 사용자 ID
     * @param username 사용자명
     * @param email 이메일
     */
    void handleNewUser(String userId, String username, String email);
    
    /**
     * 사용자 레벨에 따른 퀴즈 추천 조정
     * 
     * <p>사용자가 레벨업했을 때 호출되어 추천 퀴즈의 난이도를 조정합니다.</p>
     * 
     * @param userId 사용자 ID
     * @param level 현재 레벨
     */
    void adjustQuizRecommendationByLevel(String userId, int level);
} 