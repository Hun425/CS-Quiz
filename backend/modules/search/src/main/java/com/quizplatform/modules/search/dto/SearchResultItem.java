package com.quizplatform.modules.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 검색 결과 아이템 DTO
 * <p>
 * 검색 결과의 개별 아이템 정보를 담는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultItem {
    private String id;
    private String type;
    private String title;
    private String description;
    private Double score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
    private String url;
    private String[] highlightedFields;
    private String[] tags;
}