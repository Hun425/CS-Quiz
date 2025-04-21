package com.quizplatform.modules.quiz.service;

import com.quizplatform.modules.quiz.dto.DailyQuizResponse;
import com.quizplatform.modules.quiz.dto.QuizDetailResponse;
import com.quizplatform.modules.quiz.dto.QuizRequest;
import com.quizplatform.modules.quiz.dto.QuizResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 퀴즈 서비스 인터페이스
 * <p>
 * 퀴즈 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * </p>
 */
public interface QuizService {

    /**
     * 퀴즈 ID로 퀴즈 정보를 조회합니다.
     *
     * @param quizId 퀴즈 ID
     * @return 퀴즈 상세 정보
     */
    QuizDetailResponse getQuizById(Long quizId);

    /**
     * 조건에 맞는 퀴즈 목록을 조회합니다.
     *
     * @param filters 필터 조건 (태그, 난이도, 카테고리 등)
     * @param pageable 페이지네이션 정보
     * @return 퀴즈 목록
     */
    Page<QuizResponse> getQuizzes(Map<String, Object> filters, Pageable pageable);

    /**
     * 새로운 퀴즈를 생성합니다.
     *
     * @param quizRequest 퀴즈 생성 정보
     * @return 생성된 퀴즈 정보
     */
    QuizDetailResponse createQuiz(QuizRequest quizRequest);

    /**
     * 기존 퀴즈를 업데이트합니다.
     *
     * @param quizId 퀴즈 ID
     * @param quizRequest 퀴즈 업데이트 정보
     * @return 업데이트된 퀴즈 정보
     */
    QuizDetailResponse updateQuiz(Long quizId, QuizRequest quizRequest);

    /**
     * 퀴즈를 삭제합니다.
     *
     * @param quizId 퀴즈 ID
     */
    void deleteQuiz(Long quizId);

    /**
     * 오늘의 퀴즈를 조회합니다.
     *
     * @return 오늘의 퀴즈
     */
    DailyQuizResponse getDailyQuiz();

    /**
     * 사용자에게 추천 퀴즈를 제공합니다.
     *
     * @param userId 사용자 ID
     * @param limit 조회할 퀴즈 수
     * @return 추천 퀴즈 목록
     */
    List<QuizResponse> getRecommendedQuizzes(Long userId, int limit);

    /**
     * 사용자 퀴즈 풀이 결과를 저장합니다.
     *
     * @param quizId 퀴즈 ID
     * @param attemptRequest 퀴즈 풀이 정보
     * @return 저장된 퀴즈 풀이 결과
     */
    // QuizAttemptResponse saveQuizAttempt(Long quizId, QuizAttemptRequest attemptRequest);
}