package com.quizplatform.apigateway.client;

import com.quizplatform.core.client.ModuleClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tag 모듈 클라이언트
 * <p>
 * API Gateway에서 Tag 모듈과 통신하기 위한 클라이언트
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TagModuleClient {

    @Qualifier("tagModuleClient")
    private final ModuleClient client;
    
    @Value("${module.services.tag}")
    private String tagServiceUrl;
    
    /**
     * 태그 ID로 태그 정보를 조회합니다.
     *
     * @param tagId 태그 ID
     * @return 태그 정보
     */
    public Object getTagById(Long tagId) {
        log.debug("Fetching tag with ID: {} from Tag module", tagId);
        return client.get("/api/v1/tags/" + tagId, Object.class);
    }
    
    /**
     * 모든 태그 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 태그 목록
     */
    public Object getAllTags(Pageable pageable) {
        log.debug("Fetching all tags from Tag module");
        
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageable.getPageNumber());
        params.put("size", pageable.getPageSize());
        
        return client.get("/api/v1/tags", params, Object.class);
    }
    
    /**
     * 인기 태그를 조회합니다.
     *
     * @param limit 조회할 태그 수
     * @return 인기 태그 목록
     */
    public Object getPopularTags(int limit) {
        log.debug("Fetching popular tags from Tag module with limit: {}", limit);
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        return client.get("/api/v1/tags/popular", params, Object.class);
    }
    
    /**
     * 태그 이름으로 태그를 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 태그 목록
     */
    public Object searchTags(String keyword) {
        log.debug("Searching tags from Tag module with keyword: {}", keyword);
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        return client.get("/api/v1/tags/search", params, Object.class);
    }
    
    /**
     * 컨텐츠에 태그를 연결합니다.
     *
     * @param contentType 컨텐츠 타입
     * @param contentId 컨텐츠 ID
     * @param tagIds 태그 ID 목록
     * @return 처리 결과
     */
    public Object assignTagsToContent(String contentType, Long contentId, List<Long> tagIds) {
        log.debug("Assigning tags to content type: {} and ID: {} through Tag module", contentType, contentId);
        return client.post("/api/v1/tags/content/" + contentType + "/" + contentId, tagIds, Object.class);
    }
}