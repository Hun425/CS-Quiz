package com.quizplatform.modules.search.api.impl;

import com.quizplatform.modules.search.api.SearchModuleApi;
import com.quizplatform.modules.search.dto.SearchResponse;
import com.quizplatform.modules.search.dto.SearchSuggestionResponse;
import com.quizplatform.modules.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 검색 모듈 API 구현체
 * <p>
 * SearchModuleApi 인터페이스를 구현하여 검색 관련 API를 제공합니다.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class SearchModuleApiImpl implements SearchModuleApi {

    private final SearchService searchService;

    @Override
    public ResponseEntity<?> search(String query, String[] types, Map<String, Object> filters, Pageable pageable) {
        log.debug("Performing search with query: {}, types: {}", query, types);
        SearchResponse searchResults = searchService.search(query, types, filters, pageable);
        return ResponseEntity.ok(searchResults);
    }

    @Override
    public ResponseEntity<?> searchQuizzes(String query, Map<String, Object> filters, Pageable pageable) {
        log.debug("Searching quizzes with query: {}", query);
        SearchResponse searchResults = searchService.searchQuizzes(query, filters, pageable);
        return ResponseEntity.ok(searchResults);
    }

    @Override
    public ResponseEntity<?> searchTags(String query, Pageable pageable) {
        log.debug("Searching tags with query: {}", query);
        SearchResponse searchResults = searchService.searchTags(query, pageable);
        return ResponseEntity.ok(searchResults);
    }

    @Override
    public ResponseEntity<?> searchUsers(String query, Pageable pageable) {
        log.debug("Searching users with query: {}", query);
        SearchResponse searchResults = searchService.searchUsers(query, pageable);
        return ResponseEntity.ok(searchResults);
    }

    @Override
    public ResponseEntity<?> reindex(String type) {
        log.debug("Reindexing with type: {}", type);
        boolean success = searchService.reindex(type);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @Override
    public ResponseEntity<?> getSearchSuggestions(String query, int limit) {
        log.debug("Getting search suggestions for query: {}", query);
        List<SearchSuggestionResponse> suggestions = searchService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }
}