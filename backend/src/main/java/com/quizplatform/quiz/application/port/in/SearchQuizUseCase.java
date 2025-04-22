package com.quizplatform.quiz.application.port.in;

import com.quizplatform.quiz.application.port.in.command.SearchQuizCommand;
import com.quizplatform.quiz.domain.model.Quiz;

import java.util.List;

/**
 * 퀴즈 검색 유스케이스
 */
public interface SearchQuizUseCase {
    /**
     * 퀴즈를 검색합니다.
     *
     * @param command 검색 명령
     * @return 검색된 퀴즈 목록
     */
    List<Quiz> searchQuizzes(SearchQuizCommand command);
    
    /**
     * 추천 퀴즈를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param limit 조회할 최대 개수
     * @return 추천 퀴즈 목록
     */
    List<Quiz> getRecommendedQuizzes(Long userId, int limit);
}