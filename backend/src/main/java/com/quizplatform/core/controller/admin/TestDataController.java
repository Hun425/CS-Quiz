package com.quizplatform.core.controller.admin;

import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.utils.QuizDataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 테스트 데이터 관리 컨트롤러
 * 
 * <p>성능 테스트 등을 위한 더미 데이터 생성 및 관리 API를 제공합니다.</p>
 * <p>이 컨트롤러는 개발/테스트 환경에서만 활성화되어야 합니다.</p>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "테스트 데이터 관리", description = "테스트 목적의 더미 데이터 생성 및 관리 API")
@PreAuthorize("hasRole('ADMIN')")
public class TestDataController {

    private final QuizDataGenerator quizDataGenerator;
    
    /**
     * 더미 퀴즈 데이터 생성 API
     * 
     * @param count 생성할 퀴즈 수
     * @return 생성된 퀴즈 ID 목록
     */
    @Operation(summary = "더미 퀴즈 생성", description = "지정된 개수만큼 더미 퀴즈 데이터를 생성합니다")
    @PostMapping("/generate-quizzes")
    public ResponseEntity<CommonApiResponse<List<Long>>> generateQuizzes(@RequestParam int count) {
        try {
            log.info("더미 퀴즈 생성 요청: {}개", count);
            List<Long> quizIds = quizDataGenerator.generateDummyQuizzes(count);
            return ResponseEntity.ok(CommonApiResponse.success(quizIds, count + "개의 더미 퀴즈가 생성되었습니다"));
        } catch (Exception e) {
            log.error("더미 퀴즈 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(CommonApiResponse.error("더미 퀴즈 생성 실패: " + e.getMessage(), "QUIZ_GENERATION_ERROR"));
        }
    }
    
    /**
     * 캐시 워밍업 API
     * 
     * <p>모든 퀴즈 데이터를 미리 캐시에 로드합니다.</p>
     * 
     * @return 워밍업 결과
     */
    @Operation(summary = "캐시 워밍업", description = "Redis 캐시를 미리 워밍업합니다")
    @PostMapping("/warmup-cache")
    public ResponseEntity<CommonApiResponse<Map<String, String>>> warmupCache() {
        try {
            log.info("캐시 워밍업 요청");
            quizDataGenerator.warmupCache();
            return ResponseEntity.ok(CommonApiResponse.success(
                Map.of("status", "success"),
                "캐시 워밍업이 완료되었습니다"
            ));
        } catch (Exception e) {
            log.error("캐시 워밍업 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(CommonApiResponse.error("캐시 워밍업 실패: " + e.getMessage(), "CACHE_WARMUP_ERROR"));
        }
    }
    
    /**
     * 더미 데이터 삭제 API
     * 
     * <p>생성된 모든 더미 데이터를 삭제합니다.</p>
     * 
     * @return 삭제 결과
     */
    @Operation(summary = "더미 데이터 삭제", description = "생성된 모든 더미 데이터를 삭제합니다")
    @DeleteMapping("/clear-data")
    public ResponseEntity<CommonApiResponse<Map<String, String>>> clearDummyData() {
        try {
            log.info("더미 데이터 삭제 요청");
            quizDataGenerator.clearAllDummyData();
            return ResponseEntity.ok(CommonApiResponse.success(
                Map.of("status", "success"),
                "모든 더미 데이터가 삭제되었습니다"
            ));
        } catch (Exception e) {
            log.error("더미 데이터 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(CommonApiResponse.error("더미 데이터 삭제 실패: " + e.getMessage(), "DATA_CLEAR_ERROR"));
        }
    }
} 