package com.quizplatform.modules.quiz.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 퀴즈 모듈 API 인터페이스
 * <p>
 * 퀴즈 모듈이 다른 모듈에 제공하는 API를 정의합니다.
 * </p>
 */
@RequestMapping("/api/v1/quizzes")
public interface QuizModuleApi {

    /**
     * 퀴즈 ID로 퀴즈 정보를 조회합니다.
     *
     * @param quizId 퀴즈 ID
     * @return 퀴즈 정보
     */
    @GetMapping("/{quizId}")
    ResponseEntity<?> getQuizById(@PathVariable Long quizId);

    /**
     * 조건에 맞는 퀴즈 목록을 조회합니다.
     *
     * @param filters 필터 조건 (태그, 난이도, 카테고리 등)
     * @param pageable 페이지네이션 정보
     * @return 퀴즈 목록
     */
    @GetMapping
    ResponseEntity<?> getQuizzes(@RequestParam Map<String, Object> filters, Pageable pageable);

    /**
     * 새로운 퀴즈를 생성합니다.
     *
     * @param quizDto 퀴즈 생성 정보
     * @return 생성된 퀴즈 정보
     */
    @PostMapping
    ResponseEntity<?> createQuiz(@RequestBody Object quizDto);

    /**
     * 기존 퀴즈를 업데이트합니다.
     *
     * @param quizId 퀴즈 ID
     * @param quizDto 퀴즈 업데이트 정보
     * @return 업데이트된 퀴즈 정보
     */
    @PutMapping("/{quizId}")
    ResponseEntity<?> updateQuiz(@PathVariable Long quizId, @RequestBody Object quizDto);

    /**
     * 퀴즈를 삭제합니다.
     *
     * @param quizId 퀴즈 ID
     * @return 성공 여부
     */
    @DeleteMapping("/{quizId}")
    ResponseEntity<?> deleteQuiz(@PathVariable Long quizId);

    /**
     * 오늘의 퀴즈를 조회합니다.
     *
     * @return 오늘의 퀴즈
     */
    @GetMapping("/daily")
    ResponseEntity<?> getDailyQuiz();

    /**
     * 사용자에게 추천 퀴즈를 제공합니다.
     *
     * @param userId 사용자 ID
     * @param limit 조회할 퀴즈 수
     * @return 추천 퀴즈 목록
     */
    @GetMapping("/recommendations")
    ResponseEntity<?> getRecommendedQuizzes(@RequestParam Long userId, @RequestParam(defaultValue = "10") int limit);

    /**
     * 사용자 퀴즈 풀이 결과를 저장합니다.
     *
     * @param quizId 퀴즈 ID
     * @param attemptDto 퀴즈 풀이 정보
     * @return 저장된 퀴즈 풀이 결과
     */
    @PostMapping("/{quizId}/attempts")
    ResponseEntity<?> saveQuizAttempt(@PathVariable Long quizId, @RequestBody Object attemptDto);
}