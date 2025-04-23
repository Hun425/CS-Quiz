package com.quizplatform.quiz.presentation.controller;

import com.quizplatform.quiz.application.dto.QuizCreateRequest;
import com.quizplatform.quiz.application.dto.QuizResponse;
import com.quizplatform.quiz.application.dto.QuizUpdateRequest;
import com.quizplatform.quiz.application.service.QuizApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Quiz Controller", description = "퀴즈 관련 API를 제공합니다")
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
    @Operation(summary = "퀴즈 생성", description = "새로운 퀴즈를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "퀴즈가 성공적으로 생성되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다.")
    })
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(
            @Parameter(description = "퀴즈 생성 요청 데이터", required = true)
            @RequestBody @Valid QuizCreateRequest request) {
        QuizResponse created = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 퀴즈 ID로 조회
     * 
     * @param id 퀴즈 ID
     * @return 퀴즈 정보
     */
    @Operation(summary = "퀴즈 상세 조회", description = "ID로 퀴즈 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 상세 정보가 성공적으로 조회되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuiz(
            @Parameter(description = "조회할 퀴즈의 ID", required = true)
            @PathVariable Long id) {
        QuizResponse quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * 퀴즈 목록 페이지별 조회
     * 
     * @param pageable 페이지 정보
     * @return 퀴즈 목록
     */
    @Operation(summary = "퀴즈 목록 조회", description = "퀴즈 목록을 페이지별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 목록이 성공적으로 조회되었습니다.")
    })
    @GetMapping
    public ResponseEntity<Page<QuizResponse>> getQuizzes(
            @Parameter(description = "페이지 요청 정보 (크기, 번호, 정렬)")
            Pageable pageable) {
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
    @Operation(summary = "카테고리별 퀴즈 조회", description = "특정 카테고리에 속한 퀴즈 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 카테고리를 찾을 수 없습니다.")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByCategory(
            @Parameter(description = "조회할 카테고리명", required = true) @PathVariable String category,
            @Parameter(description = "페이지 요청 정보") Pageable pageable) {
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
    @Operation(summary = "난이도별 퀴즈 조회", description = "특정 난이도의 퀴즈 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "난이도별 퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 난이도 값입니다.")
    })
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByDifficulty(
            @Parameter(description = "조회할 난이도 (1-5)", required = true, example = "3")
            @PathVariable int difficulty, 
            @Parameter(description = "페이지 요청 정보") Pageable pageable) {
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
    @Operation(summary = "생성자별 퀴즈 조회", description = "특정 사용자가 생성한 퀴즈 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자가 생성한 퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/creator/{userId}")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByCreator(
            @Parameter(description = "퀴즈 생성자 ID", required = true)
            @PathVariable Long userId, 
            @Parameter(description = "페이지 요청 정보") Pageable pageable) {
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
    @Operation(summary = "퀴즈 정보 수정", description = "기존 퀴즈의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈가 성공적으로 수정되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @Parameter(description = "수정할 퀴즈의 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "퀴즈 수정 요청 데이터", required = true)
            @RequestBody @Valid QuizUpdateRequest request) {
        QuizResponse updated = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 퀴즈 삭제
     * 
     * @param id 퀴즈 ID
     * @return 삭제 결과 응답
     */
    @Operation(summary = "퀴즈 삭제", description = "지정된 ID의 퀴즈를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "퀴즈가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(
            @Parameter(description = "삭제할 퀴즈의 ID", required = true)
            @PathVariable Long id) {
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
    @Operation(summary = "퀴즈 공개 상태 변경", description = "퀴즈의 공개/비공개 상태를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 공개 상태가 성공적으로 변경되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @PatchMapping("/{id}/publish")
    public ResponseEntity<QuizResponse> setQuizPublishStatus(
            @Parameter(description = "상태를 변경할 퀴즈의 ID", required = true)
            @PathVariable Long id, 
            @Parameter(description = "공개 여부 (true: 공개, false: 비공개)", required = true)
            @RequestParam boolean publish) {
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
    @Operation(summary = "퀴즈 활성화 상태 변경", description = "퀴즈의 활성화/비활성화 상태를 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 활성화 상태가 성공적으로 변경되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @PatchMapping("/{id}/active")
    public ResponseEntity<QuizResponse> setQuizActiveStatus(
            @Parameter(description = "상태를 변경할 퀴즈의 ID", required = true)
            @PathVariable Long id, 
            @Parameter(description = "활성화 여부 (true: 활성화, false: 비활성화)", required = true)
            @RequestParam boolean active) {
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
    @Operation(summary = "퀴즈 검색", description = "키워드로 퀴즈를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 결과가 성공적으로 조회되었습니다.")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<QuizResponse>> searchQuizzes(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword, 
            @Parameter(description = "페이지 요청 정보")
            Pageable pageable) {
        Page<QuizResponse> results = quizService.searchQuizzes(keyword, pageable);
        return ResponseEntity.ok(results);
    }
} 