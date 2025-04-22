package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.domain.model.Quiz;

import java.util.List;
import java.util.Optional;

/**
 * 퀴즈 조회 유스케이스
 */
public interface GetQuizUseCase {
    /**
     * ID로 퀴즈를 조회합니다.
     *
     * @param quizId 퀴즈 ID
     * @return 퀴즈 (Optional)
     */
    Optional<Quiz> getQuizById(Long quizId);
    
    /**
     * 문제를 포함한 퀴즈를 조회합니다.
     *
     * @param quizId 퀴즈 ID
     * @return 퀴즈 (Optional)
     */
    Optional<Quiz> getQuizWithQuestions(Long quizId);
    
    /**
     * 현재 유효한 데일리 퀴즈를 조회합니다.
     *
     * @return 데일리 퀴즈 (Optional)
     */
    Optional<Quiz> getCurrentDailyQuiz();
    
    /**
     * 특정 태그의 퀴즈 목록을 조회합니다.
     *
     * @param tagId 태그 ID
     * @param limit 조회할 최대 개수
     * @param offset 오프셋
     * @return 퀴즈 목록
     */
    List<Quiz> getQuizzesByTag(Long tagId, int limit, int offset);
    
    /**
     * 사용자가 만든 퀴즈 목록을 조회합니다.
     *
     * @param creatorId 생성자 ID
     * @param limit 조회할 최대 개수
     * @param offset 오프셋
     * @return 퀴즈 목록
     */
    List<Quiz> getQuizzesByCreator(Long creatorId, int limit, int offset);
}