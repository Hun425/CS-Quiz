package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.domain.model.QuizAttempt;

import java.util.List;
import java.util.Optional;

/**
 * 퀴즈 시도 조회 유스케이스
 */
public interface GetQuizAttemptUseCase {
    /**
     * ID로 퀴즈 시도를 조회합니다.
     *
     * @param quizAttemptId 퀴즈 시도 ID
     * @return 퀴즈 시도 (Optional)
     */
    Optional<QuizAttempt> getQuizAttemptById(Long quizAttemptId);
    
    /**
     * 사용자의 특정 퀴즈 시도 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param quizId 퀴즈 ID
     * @return 퀴즈 시도 목록
     */
    List<QuizAttempt> getUserQuizAttempts(Long userId, Long quizId);
    
    /**
     * 사용자의 모든 퀴즈 시도 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param limit 조회할 최대 개수
     * @param offset 오프셋
     * @return 퀴즈 시도 목록
     */
    List<QuizAttempt> getAllUserQuizAttempts(Long userId, int limit, int offset);
}