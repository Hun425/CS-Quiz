package com.quizplatform.modules.search.service;

import com.quizplatform.modules.search.dto.SearchResponse;
import com.quizplatform.modules.search.dto.SearchSuggestionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 검색 서비스 인터페이스
 * <p>
 * 검색 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * </p>
 */
public interface SearchService {

    /**
     * 통합 검색을 수행합니다.
     *
     * @param query 검색어
     * @param types 검색 대상 타입 (퀴즈, 태그, 사용자 등)
     * @param filters 추가 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 검색 결과
     */
    SearchResponse search(String query, String[] types, Map<String, Object> filters, Pageable pageable);

    /**
     * 퀴즈 검색을 수행합니다.
     *
     * @param query 검색어
     * @param filters 추가 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 퀴즈 검색 결과
     */
    SearchResponse searchQuizzes(String query, Map<String, Object> filters, Pageable pageable);

    /**
     * 태그 검색을 수행합니다.
     *
     * @param query 검색어
     * @param pageable 페이지네이션 정보
     * @return 태그 검색 결과
     */
    SearchResponse searchTags(String query, Pageable pageable);

    /**
     * 사용자 검색을 수행합니다.
     *
     * @param query 검색어
     * @param pageable 페이지네이션 정보
     * @return 사용자 검색 결과
     */
    SearchResponse searchUsers(String query, Pageable pageable);

    /**
     * 검색 인덱스를 재구축합니다.
     *
     * @param type 재구축할 인덱스 타입 (null인 경우 모든 인덱스)
     * @return 작업 성공 여부
     */
    boolean reindex(String type);

    /**
     * 검색어 자동완성 추천을 제공합니다.
     *
     * @param query 입력 중인 검색어
     * @param limit 추천 개수
     * @return 검색어 추천 목록
     */
    List<SearchSuggestionResponse> getSearchSuggestions(String query, int limit);
}