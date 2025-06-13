package com.quizplatform.quiz.application.dto;

import lombok.Builder;

/**
 * 태그 검색 요청 DTO
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Builder
public record TagSearchRequest(
    
    /**
     * 검색 키워드
     */
    String keyword,
    
    /**
     * 계층 레벨 필터 (null이면 모든 레벨)
     */
    Integer level,
    
    /**
     * 활성화된 태그만 검색할지 여부
     */
    Boolean activeOnly,
    
    /**
     * 부모 태그 ID (해당 부모의 하위 태그만 검색)
     */
    Long parentId,
    
    /**
     * 최소 사용 횟수
     */
    Integer minUsageCount
) {
    
    /**
     * 기본값이 적용된 TagSearchRequest 생성
     * 
     * @return 기본값이 설정된 검색 요청
     */
    public TagSearchRequest withDefaults() {
        return TagSearchRequest.builder()
                .keyword(this.keyword)
                .level(this.level)
                .activeOnly(this.activeOnly != null ? this.activeOnly : true)
                .parentId(this.parentId)
                .minUsageCount(this.minUsageCount != null ? this.minUsageCount : 0)
                .build();
    }
}