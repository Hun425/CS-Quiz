package com.quizplatform.modules.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 검색 응답 DTO
 * <p>
 * 검색 결과를 담는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private Long totalHits;
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Map<String, Long> facets;
    private String query;
    private String[] types;
    private Map<String, Object> filters;
    private List<SearchResultItem> items;
}