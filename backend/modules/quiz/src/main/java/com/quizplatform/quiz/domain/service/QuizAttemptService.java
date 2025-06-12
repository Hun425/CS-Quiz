package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.application.dto.*;
import com.quizplatform.quiz.domain.model.QuizAttempt;

import java.util.List;

/**
 * 퀴즈 시도 서비스 인터페이스
 * 
 * <p>퀴즈 시도 생성, 답변 제출, 완료 처리 등의 비즈니스 로직을 정의합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface QuizAttemptService {
    
    /**
     * 퀴즈 시도 시작
     * 
     * @param request 퀴즈 시도 요청
     * @param userId 사용자 ID
     * @return 생성된 퀴즈 시도
     */
    QuizAttemptResponse startQuizAttempt(QuizAttemptRequest request, Long userId);
    
    /**
     * 퀴즈 답변 제출 및 완료 처리
     * 
     * @param request 퀴즈 제출 요청
     * @param userId 사용자 ID
     * @return 완료된 퀴즈 시도
     */
    QuizAttemptResponse submitQuizAttempt(QuizSubmitRequest request, Long userId);
    
    /**
     * 사용자의 퀴즈 시도 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 퀴즈 시도 목록
     */
    List<QuizAttemptResponse> getUserQuizAttempts(Long userId);
    
    /**
     * 특정 퀴즈의 시도 목록 조회
     * 
     * @param quizId 퀴즈 ID
     * @return 퀴즈 시도 목록
     */
    List<QuizAttemptResponse> getQuizAttempts(Long quizId);
    
    /**
     * 퀴즈 시도 상세 조회
     * 
     * @param attemptId 시도 ID
     * @param userId 사용자 ID
     * @return 퀴즈 시도 상세
     */
    QuizAttemptResponse getQuizAttempt(Long attemptId, Long userId);
    
    /**
     * 사용자의 특정 퀴즈 최근 시도 조회
     * 
     * @param quizId 퀴즈 ID
     * @param userId 사용자 ID
     * @return 최근 퀴즈 시도
     */
    QuizAttemptResponse getLatestQuizAttempt(Long quizId, Long userId);
}