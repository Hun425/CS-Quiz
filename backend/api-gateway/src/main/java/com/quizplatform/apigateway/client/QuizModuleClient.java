package com.quizplatform.apigateway.client;

import com.quizplatform.core.client.ModuleClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Quiz 모듈 클라이언트
 * <p>
 * API Gateway에서 Quiz 모듈과 통신하기 위한 클라이언트
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuizModuleClient {

    @Qualifier("quizModuleClient")
    private final ModuleClient client;
    
    @Value("${module.services.quiz}")
    private String quizServiceUrl;
    
    /**
     * 퀴즈 ID로 퀴즈 정보를 조회합니다.
     *
     * @param quizId 퀴즈 ID
     * @return 퀴즈 정보
     */
    public Object getQuizById(Long quizId) {
        log.debug("Fetching quiz with ID: {} from Quiz module", quizId);
        return client.get("/api/v1/quizzes/" + quizId, Object.class);
    }
    
    /**
     * 조건에 맞는 퀴즈 목록을 조회합니다.
     *
     * @param filters 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 퀴즈 목록
     */
    public Object getQuizzes(Map<String, Object> filters, Pageable pageable) {
        log.debug("Fetching quizzes from Quiz module with filters: {}", filters);
        
        Map<String, Object> params = new HashMap<>(filters);
        params.put("page", pageable.getPageNumber());
        params.put("size", pageable.getPageSize());
        
        return client.get("/api/v1/quizzes", params, Object.class);
    }
    
    /**
     * 새로운 퀴즈를 생성합니다.
     *
     * @param quizRequest 퀴즈 생성 정보
     * @return 생성된 퀴즈 정보
     */
    public Object createQuiz(Object quizRequest) {
        log.debug("Creating quiz through Quiz module");
        return client.post("/api/v1/quizzes", quizRequest, Object.class);
    }
    
    /**
     * 오늘의 퀴즈를 조회합니다.
     *
     * @return 오늘의 퀴즈
     */
    public Object getDailyQuiz() {
        log.debug("Fetching daily quiz from Quiz module");
        return client.get("/api/v1/quizzes/daily", Object.class);
    }
    
    /**
     * 사용자에게 추천 퀴즈를 제공합니다.
     *
     * @param userId 사용자 ID
     * @param limit 조회할 퀴즈 수
     * @return 추천 퀴즈 목록
     */
    public Object getRecommendedQuizzes(Long userId, int limit) {
        log.debug("Fetching recommended quizzes from Quiz module for user: {}", userId);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("limit", limit);
        return client.get("/api/v1/quizzes/recommendations", params, Object.class);
    }
}