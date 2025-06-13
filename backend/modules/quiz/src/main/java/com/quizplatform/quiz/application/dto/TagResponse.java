package com.quizplatform.quiz.application.dto;

import com.quizplatform.quiz.domain.model.Tag;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 태그 응답 DTO
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Builder
public record TagResponse(
    
    /**
     * 태그 ID
     */
    Long id,
    
    /**
     * 태그 이름
     */
    String name,
    
    /**
     * 태그 설명
     */
    String description,
    
    /**
     * 부모 태그 정보
     */
    ParentTagInfo parent,
    
    /**
     * 자식 태그 목록 (간단 정보)
     */
    List<SimpleTagInfo> children,
    
    /**
     * 계층 레벨
     */
    int level,
    
    /**
     * 활성화 상태
     */
    boolean active,
    
    /**
     * 사용 횟수
     */
    int usageCount,
    
    /**
     * 전체 경로
     */
    String fullPath,
    
    /**
     * 생성 시간
     */
    LocalDateTime createdAt,
    
    /**
     * 수정 시간
     */
    LocalDateTime updatedAt
) {
    
    /**
     * Tag 엔티티로부터 TagResponse 생성
     * 
     * @param tag 태그 엔티티
     * @return TagResponse
     */
    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .parent(tag.getParent() != null ? ParentTagInfo.from(tag.getParent()) : null)
                .children(tag.getChildren().stream()
                         .map(SimpleTagInfo::from)
                         .collect(Collectors.toList()))
                .level(tag.getLevel())
                .active(tag.isActive())
                .usageCount(tag.getUsageCount())
                .fullPath(tag.getFullPath())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
    
    /**
     * 부모 태그 정보
     */
    @Builder
    public record ParentTagInfo(
        Long id,
        String name,
        int level,
        String fullPath
    ) {
        public static ParentTagInfo from(Tag parent) {
            return ParentTagInfo.builder()
                    .id(parent.getId())
                    .name(parent.getName())
                    .level(parent.getLevel())
                    .fullPath(parent.getFullPath())
                    .build();
        }
    }
    
    /**
     * 간단한 태그 정보 (자식 태그용)
     */
    @Builder
    public record SimpleTagInfo(
        Long id,
        String name,
        boolean active,
        int usageCount
    ) {
        public static SimpleTagInfo from(Tag tag) {
            return SimpleTagInfo.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    .active(tag.isActive())
                    .usageCount(tag.getUsageCount())
                    .build();
        }
    }
}