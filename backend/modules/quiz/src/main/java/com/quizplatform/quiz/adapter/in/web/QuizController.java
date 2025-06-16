package com.quizplatform.quiz.adapter.in.web;

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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.quizplatform.common.auth.CurrentUser;
import com.quizplatform.common.auth.CurrentUserInfo;
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
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Tag(name = "Quiz Controller", description = "퀴즈 관련 API를 제공합니다")
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
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
            @RequestBody @Valid QuizCreateRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
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
            @RequestBody @Valid QuizUpdateRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
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
            @PathVariable Long id,
            @CurrentUser CurrentUserInfo currentUser) {
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
            @RequestParam boolean publish,
            @CurrentUser CurrentUserInfo currentUser) {
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
            @RequestParam boolean active,
            @CurrentUser CurrentUserInfo currentUser) {
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
    
    // ===== Tag 기반 퀴즈 조회 API =====
    
    /**
     * 특정 태그의 퀴즈 목록 조회
     * 
     * @param tagId 태그 ID
     * @param pageable 페이지 정보
     * @return 태그별 퀴즈 목록
     */
    @Operation(summary = "태그별 퀴즈 조회", description = "특정 태그에 연결된 퀴즈 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그별 퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "비활성화된 태그입니다.")
    })
    @GetMapping("/tags/{tagId}")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByTag(
            @Parameter(description = "조회할 태그의 ID", required = true)
            @PathVariable Long tagId,
            @Parameter(description = "페이지 요청 정보")
            Pageable pageable) {
        Page<QuizResponse> quizzes = quizService.getQuizzesByTag(tagId, pageable);
        return ResponseEntity.ok(quizzes);
    }
    
    /**
     * 여러 태그 조건으로 퀴즈 목록 조회
     * 
     * @param tagIds 태그 ID 목록
     * @param operator 논리 연산자 (AND/OR)
     * @param pageable 페이지 정보
     * @return 태그별 퀴즈 목록
     */
    @Operation(summary = "다중 태그 퀴즈 조회", description = "여러 태그 조건으로 퀴즈 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "다중 태그 퀴즈 목록이 성공적으로 조회되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 태그 ID 목록 또는 논리 연산자입니다.")
    })
    @GetMapping("/tags")
    public ResponseEntity<Page<QuizResponse>> getQuizzesByTags(
            @Parameter(description = "태그 ID 목록 (콤마로 구분)", required = true, example = "1,2,3")
            @RequestParam List<Long> tagIds,
            @Parameter(description = "논리 연산자 (AND: 모든 태그 포함, OR: 하나 이상 태그 포함)", example = "AND")
            @RequestParam(defaultValue = "AND") String operator,
            @Parameter(description = "페이지 요청 정보")
            Pageable pageable) {
        Page<QuizResponse> quizzes = quizService.getQuizzesByTags(tagIds, operator, pageable);
        return ResponseEntity.ok(quizzes);
    }
    
    /**
     * 고급 검색 (키워드 + 태그 + 카테고리 + 난이도 조합)
     * 
     * @param keyword 검색 키워드 (선택적)
     * @param tagIds 태그 ID 목록 (선택적)
     * @param category 카테고리 (선택적)
     * @param difficulty 난이도 (선택적)
     * @param pageable 페이지 정보
     * @return 고급 검색 결과
     */
    @Operation(summary = "퀴즈 고급 검색", description = "키워드, 태그, 카테고리, 난이도를 조합하여 퀴즈를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "고급 검색 결과가 성공적으로 조회되었습니다.")
    })
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<QuizResponse>> advancedSearchQuizzes(
            @Parameter(description = "검색 키워드 (선택적)", example = "Java")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "태그 ID 목록 (선택적, 콤마로 구분)", example = "1,2,3")
            @RequestParam(required = false) List<Long> tagIds,
            @Parameter(description = "카테고리 (선택적)", example = "프로그래밍")
            @RequestParam(required = false) String category,
            @Parameter(description = "난이도 (선택적, 1-5)", example = "3")
            @RequestParam(required = false) Integer difficulty,
            @Parameter(description = "페이지 요청 정보")
            Pageable pageable) {
        Page<QuizResponse> results = quizService.advancedSearchQuizzes(keyword, tagIds, category, difficulty, pageable);
        return ResponseEntity.ok(results);
    }
    
    // ===== Quiz-Tag 관계 관리 API =====
    
    /**
     * 퀴즈의 태그 목록 업데이트
     * 
     * @param quizId 퀴즈 ID
     * @param tagIds 새로운 태그 ID 목록
     * @param currentUser 현재 사용자 정보
     * @return 업데이트된 퀴즈 정보
     */
    @Operation(summary = "퀴즈 태그 업데이트", description = "퀴즈의 태그 목록을 새로운 목록으로 교체합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 태그가 성공적으로 업데이트되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 태그 ID 목록입니다."),
            @ApiResponse(responseCode = "404", description = "퀴즈 또는 태그를 찾을 수 없습니다.")
    })
    @PutMapping("/{quizId}/tags")
    public ResponseEntity<QuizResponse> updateQuizTags(
            @Parameter(description = "태그를 업데이트할 퀴즈의 ID", required = true)
            @PathVariable Long quizId,
            @Parameter(description = "새로운 태그 ID 목록", required = true)
            @RequestBody List<Long> tagIds,
            @CurrentUser CurrentUserInfo currentUser) {
        QuizResponse updated = quizService.updateQuizTags(quizId, tagIds);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 퀴즈에 태그 추가
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 추가할 태그 ID
     * @param currentUser 현재 사용자 정보
     * @return 업데이트된 퀴즈 정보
     */
    @Operation(summary = "퀴즈에 태그 추가", description = "퀴즈에 새로운 태그를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그가 성공적으로 추가되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "400", description = "태그를 추가할 수 없습니다. (최대 태그 수 초과 또는 이미 할당된 태그)"),
            @ApiResponse(responseCode = "404", description = "퀴즈 또는 태그를 찾을 수 없습니다.")
    })
    @PostMapping("/{quizId}/tags/{tagId}")
    public ResponseEntity<QuizResponse> addTagToQuiz(
            @Parameter(description = "태그를 추가할 퀴즈의 ID", required = true)
            @PathVariable Long quizId,
            @Parameter(description = "추가할 태그의 ID", required = true)
            @PathVariable Long tagId,
            @CurrentUser CurrentUserInfo currentUser) {
        QuizResponse updated = quizService.addTagToQuiz(quizId, tagId);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * 퀴즈에서 태그 제거
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 제거할 태그 ID
     * @param currentUser 현재 사용자 정보
     * @return 업데이트된 퀴즈 정보
     */
    @Operation(summary = "퀴즈에서 태그 제거", description = "퀴즈에서 특정 태그를 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그가 성공적으로 제거되었습니다.",
                    content = @Content(schema = @Schema(implementation = QuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "퀴즈를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{quizId}/tags/{tagId}")
    public ResponseEntity<QuizResponse> removeTagFromQuiz(
            @Parameter(description = "태그를 제거할 퀴즈의 ID", required = true)
            @PathVariable Long quizId,
            @Parameter(description = "제거할 태그의 ID", required = true)
            @PathVariable Long tagId,
            @CurrentUser CurrentUserInfo currentUser) {
        QuizResponse updated = quizService.removeTagFromQuiz(quizId, tagId);
        return ResponseEntity.ok(updated);
    }
} 