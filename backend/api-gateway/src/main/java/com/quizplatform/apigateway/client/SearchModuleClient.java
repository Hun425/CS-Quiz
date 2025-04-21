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
 * Search 모듈 클라이언트
 * <p>
 * API Gateway에서 Search 모듈과 통신하기 위한 클라이언트
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchModuleClient {

    @Qualifier("searchModuleClient")
    private final ModuleClient client;
    
    @Value("${module.services.search}")
    private String searchServiceUrl;
    
    /**
     * 통합 검색을 수행합니다.
     *
     * @param query 검색어
     * @param types 검색 대상 타입 (퀴즈, 태그, 사용자 등)
     * @param filters 추가 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 검색 결과
     */
    public Object search(String query, String[] types, Map<String, Object> filters, Pageable pageable) {
        log.debug("Performing search through Search module with query: {}, types: {}", query, types);
        
        Map<String, Object> params = new HashMap<>(filters);
        params.put("query", query);
        params.put("types", types);
        params.put("page", pageable.getPageNumber());
        params.put("size", pageable.getPageSize());
        
        return client.get("/api/v1/search", params, Object.class);
    }
    
    /**
     * 퀴즈 검색을 수행합니다.
     *
     * @param query 검색어
     * @param filters 추가 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 퀴즈 검색 결과
     */
    public Object searchQuizzes(String query, Map<String, Object> filters, Pageable pageable) {
        log.debug("Searching quizzes through Search module with query: {}", query);
        
        Map<String, Object> params = new HashMap<>(filters);
        params.put("query", query);
        params.put("page", pageable.getPageNumber());
        params.put("size", pageable.getPageSize());
        
        return client.get("/api/v1/search/quizzes", params, Object.class);
    }
    
    /**
     * 검색 추천어를 제공합니다.
     *
     * @param query 입력 중인 검색어
     * @param limit 추천어 개수
     * @return 검색 추천어 목록
     */
    public Object getSearchSuggestions(String query, int limit) {
        log.debug("Getting search suggestions from Search module for query: {}", query);
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("limit", limit);
        return client.get("/api/v1/search/suggestions", params, Object.class);
    }
    
    /**
     * 검색 인덱스를 재구축합니다.
     *
     * @param type 재구축할 인덱스 타입 (null인 경우 모든 인덱스)
     * @return 작업 성공 여부
     */
    public Object reindex(String type) {
        log.debug("Reindexing through Search module with type: {}", type);
        Map<String, Object> params = new HashMap<>();
        if (type != null) {
            params.put("type", type);
        }
        return client.post("/api/v1/search/reindex", params, Object.class);
    }
}