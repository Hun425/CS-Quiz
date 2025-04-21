package com.quizplatform.modules.search.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 검색 모듈 API 인터페이스
 * <p>
 * 검색 모듈이 다른 모듈에 제공하는 API를 정의합니다.
 * </p>
 */
@RequestMapping("/api/v1/search")
public interface SearchModuleApi {

    /**
     * 전체 검색을 수행합니다.
     *
     * @param query 검색어
     * @param types 검색 대상 타입 (퀴즈, 태그, 사용자 등)
     * @param filters 추가 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 검색 결과
     */
    @GetMapping
    ResponseEntity<?> search(
            @RequestParam String query,
            @RequestParam(required = false) String[] types,
            @RequestParam Map<String, Object> filters,
            Pageable pageable);

    /**
     * 퀴즈 검색을 수행합니다.
     *
     * @param query 검색어
     * @param filters 추가 필터 조건
     * @param pageable 페이지네이션 정보
     * @return 퀴즈 검색 결과
     */
    @GetMapping("/quizzes")
    ResponseEntity<?> searchQuizzes(
            @RequestParam String query,
            @RequestParam Map<String, Object> filters,
            Pageable pageable);

    /**
     * 태그 검색을 수행합니다.
     *
     * @param query 검색어
     * @param pageable 페이지네이션 정보
     * @return 태그 검색 결과
     */
    @GetMapping("/tags")
    ResponseEntity<?> searchTags(
            @RequestParam String query,
            Pageable pageable);

    /**
     * 사용자 검색을 수행합니다.
     *
     * @param query 검색어
     * @param pageable 페이지네이션 정보
     * @return 사용자 검색 결과
     */
    @GetMapping("/users")
    ResponseEntity<?> searchUsers(
            @RequestParam String query,
            Pageable pageable);

    /**
     * 엘라스틱서치 인덱스 재구성을 수동으로 시작합니다.
     *
     * @param type 인덱스 타입 (퀴즈, 태그, 사용자 등)
     * @return 작업 시작 성공 여부
     */
    @PostMapping("/reindex")
    ResponseEntity<?> reindex(@RequestParam(required = false) String type);

    /**
     * 검색 추천어를 제공합니다.
     *
     * @param query 입력 중인 검색어
     * @param limit 추천어 개수
     * @return 검색 추천어 목록
     */
    @GetMapping("/suggestions")
    ResponseEntity<?> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit);
}