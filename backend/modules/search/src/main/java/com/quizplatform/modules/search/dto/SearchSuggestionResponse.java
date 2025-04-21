package com.quizplatform.modules.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 검색 추천어 응답 DTO
 * <p>
 * 검색어 자동완성 추천 결과를 담는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionResponse {
    private String suggestion;
    private String type;
    private Double score;
    private Integer frequency;
    private Boolean isPopular;
}