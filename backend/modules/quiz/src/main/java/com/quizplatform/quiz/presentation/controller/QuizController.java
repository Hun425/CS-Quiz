package com.quizplatform.quiz.presentation.controller;

import com.quizplatform.quiz.application.dto.QuizCreateRequest;
import com.quizplatform.quiz.application.dto.QuizResponse;
import com.quizplatform.quiz.application.dto.QuizUpdateRequest;
import com.quizplatform.quiz.application.service.QuizApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 퀴즈 API 컨트롤러
 * 
 * <p>퀴즈 관련 API 엔드포인트를 정의합니다.
 * 퀴즈 생성, 조회, 수정, 삭제, 검색 등의 기능을 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizApplicationService quizService;

    /**
     * 퀴즈 생성
     * 
     * @param request 퀴즈 생성 요청 DTO
     * @return 생성된 퀴즈 정보
     */
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(@RequestBody @Valid QuizCreateRequest request) {
        QuizResponse created = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 퀴즈 ID로 조회
     * 
     * @param id 퀴즈 ID
     * @return 퀴즈 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuiz(@PathVariable Long id) {
        QuizResponse quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * 퀴즈 목록 페이지별 조회
     * 
     * @param pageable 페이지 정보
     * @return 퀴즈 목록
     */
    @GetMapping
    public ResponseEntity<Page<QuizResponse>> getQuizzes(Pageable pageable) {
        Page<QuizResponse> quizzes = quizService.getAllQuizzes(pageable);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 카테고리별 퀴즈 목록 조회
     * 
     * @param category 카테고리
     * @param pageable 페이지 정보
     * @return 카테고리별 퀴즈 목록
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByCategory(
            @PathVariable String category, Pageable pageable) {
        Page<QuizResponse> quizzes = quizService.getQuizzesByCategory(category, pageable);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 난이도별 퀴즈 목록 조회
     * 
     * @param difficulty 난이도
     * @param pageable 페이지 정보
     * @return 난이도별 퀴즈 목록
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByDifficulty(
            @PathVariable int difficulty, Pageable pageable) {
        Page<QuizResponse> quizzes = quizService.getQuizzesByDifficulty(difficulty, pageable);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 사용자가 생성한 퀴즈 목록 조회
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지 정보
     * @return 사용자가 생성한 퀴즈 목록
     */
    @GetMapping("/creator/{userId}")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByCreator(
            @PathVariable Long userId, Pageable pageable) {
        Page<QuizResponse> quizzes = quizService.getQuizzesByCreator(userId, pageable);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * 퀴즈 정보 업데이트
     * 
     * @param id 퀴즈 ID
     * @param request 업데이트 요청 DTO
     * @return 업데이트된 퀴즈 정보
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long id, @RequestBody @Valid QuizUpdateRequest request) {
        QuizResponse updated = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 퀴즈 삭제
     * 
     * @param id 퀴즈 ID
     * @return 삭제 결과 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 퀴즈 공개 상태 변경
     * 
     * @param id 퀴즈 ID
     * @param publish 공개 여부
     * @return 업데이트된 퀴즈 정보
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<QuizResponse> setQuizPublishStatus(
            @PathVariable Long id, @RequestParam boolean publish) {
        QuizResponse updated = quizService.setQuizPublishStatus(id, publish);
        return ResponseEntity.ok(updated);
    }

    /**
     * 퀴즈 활성화 상태 변경
     * 
     * @param id 퀴즈 ID
     * @param active 활성화 여부
     * @return 업데이트된 퀴즈 정보
     */
    @PatchMapping("/{id}/active")
    public ResponseEntity<QuizResponse> setQuizActiveStatus(
            @PathVariable Long id, @RequestParam boolean active) {
        QuizResponse updated = quizService.setQuizActiveStatus(id, active);
        return ResponseEntity.ok(updated);
    }

    /**
     * 퀴즈 검색
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    @GetMapping("/search")
    public ResponseEntity<Page<QuizResponse>> searchQuizzes(
            @RequestParam String keyword, Pageable pageable) {
        Page<QuizResponse> results = quizService.searchQuizzes(keyword, pageable);
        return ResponseEntity.ok(results);
    }
} 