package com.quizplatform.quiz.application.dto;

import com.quizplatform.quiz.domain.service.TagService.TagUsageStats;
import lombok.Builder;

/**
 * 태그 통계 응답 DTO
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Builder
public record TagStatsResponse(
    
    /**
     * 태그 ID
     */
    Long tagId,
    
    /**
     * 태그 이름
     */
    String tagName,
    
    /**
     * 전체 경로
     */
    String fullPath,
    
    /**
     * 사용 횟수
     */
    int usageCount,
    
    /**
     * 연결된 퀴즈 수
     */
    int connectedQuizCount,
    
    /**
     * 후손 태그 수
     */
    int descendantCount,
    
    /**
     * 인기도 순위 (사용량 기준)
     */
    int popularityRank
) {
    
    /**
     * TagUsageStats로부터 TagStatsResponse 생성
     * 
     * @param stats 태그 사용 통계
     * @param rank 인기도 순위
     * @return TagStatsResponse
     */
    public static TagStatsResponse from(TagUsageStats stats, int rank) {
        return TagStatsResponse.builder()
                .tagId(stats.tagId())
                .tagName(stats.tagName())
                .fullPath(stats.fullPath())
                .usageCount(stats.usageCount())
                .connectedQuizCount(stats.connectedQuizCount())
                .descendantCount(stats.descendantCount())
                .popularityRank(rank)
                .build();
    }
}