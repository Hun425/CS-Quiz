package com.quizplatform.quiz.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 태그 엔티티 클래스
 * 
 * <p>퀴즈 분류와 검색에 사용되는 태그를 관리합니다.
 * 주제, 기술, 난이도 등의 정보를 태그로 제공합니다.</p>
 */
@Entity
@Table(name = "tags", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    /**
     * 태그 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 태그 이름
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * 태그 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 태그 색상
     */
    @Column(length = 20)
    private String color;

    /**
     * 태그 그룹
     */
    @Column(name = "tag_group")
    private String tagGroup;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 태그 생성자
     * 
     * @param name 태그 이름
     * @param description 태그 설명
     * @param color 태그 색상
     * @param tagGroup 태그 그룹
     */
    @Builder
    public Tag(String name, String description, String color, String tagGroup) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.tagGroup = tagGroup;
    }

    /**
     * 태그 정보 업데이트
     * 
     * @param description 새 태그 설명
     * @param color 새 태그 색상
     * @param tagGroup 새 태그 그룹
     */
    public void update(String description, String color, String tagGroup) {
        this.description = description;
        this.color = color;
        this.tagGroup = tagGroup;
    }
} 