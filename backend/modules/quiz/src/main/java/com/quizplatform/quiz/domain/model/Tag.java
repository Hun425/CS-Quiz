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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 태그 도메인 모델
 * 
 * <p>계층구조를 지원하는 태그 시스템으로 최대 3단계까지 지원합니다.
 * 관리자만 태그를 생성, 수정, 삭제할 수 있으며, 퀴즈당 최대 10개까지 할당 가능합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "tags", schema = "quiz_schema", indexes = {
    @Index(name = "idx_tag_parent_id", columnList = "parent_id"),
    @Index(name = "idx_tag_level", columnList = "level"),
    @Index(name = "idx_tag_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    
    /**
     * 최대 허용 계층 깊이
     */
    public static final int MAX_HIERARCHY_LEVEL = 3;
    
    /**
     * 퀴즈당 최대 태그 수
     */
    public static final int MAX_TAGS_PER_QUIZ = 10;
    
    /**
     * 태그 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 태그 이름 (계층 내에서 고유)
     */
    @Column(nullable = false, length = 50)
    private String name;
    
    /**
     * 태그 설명
     */
    @Column(length = 255)
    private String description;
    
    /**
     * 부모 태그 (계층구조 - Adjacency List 방식)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Tag parent;
    
    /**
     * 자식 태그 목록
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> children = new ArrayList<>();
    
    /**
     * 계층 깊이 (0: 루트, 1: 1단계, 2: 2단계, 3: 3단계)
     */
    @Column(nullable = false)
    private int level = 0;
    
    /**
     * 활성화 상태
     */
    @Column(nullable = false)
    private boolean active = true;
    
    /**
     * 태그 사용 횟수 (캐시용)
     */
    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;
    
    /**
     * 태그와 연관된 퀴즈 목록
     */
    @ManyToMany
    @JoinTable(
        name = "quiz_tag_mapping",
        schema = "quiz_schema",
        joinColumns = @JoinColumn(name = "tag_id"),
        inverseJoinColumns = @JoinColumn(name = "quiz_id")
    )
    private Set<Quiz> quizzes = new HashSet<>();
    
    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 태그 생성자
     * 
     * @param name 태그 이름
     * @param description 태그 설명
     * @param parent 부모 태그 (루트 태그인 경우 null)
     */
    @Builder
    public Tag(String name, String description, Tag parent) {
        validateName(name);
        this.name = name;
        this.description = description;
        setParent(parent);
    }
    
    /**
     * 부모 태그 설정 및 레벨 계산
     * 
     * @param parent 부모 태그
     */
    public void setParent(Tag parent) {
        validateHierarchyLevel(parent);
        this.parent = parent;
        this.level = parent != null ? parent.getLevel() + 1 : 0;
        
        if (parent != null) {
            parent.children.add(this);
        }
    }
    
    /**
     * 태그 정보 업데이트
     * 
     * @param name 새 태그 이름
     * @param description 새 태그 설명
     */
    public void update(String name, String description) {
        validateName(name);
        this.name = name;
        this.description = description;
    }
    
    /**
     * 퀴즈 추가
     * 
     * @param quiz 추가할 퀴즈
     */
    public void addQuiz(Quiz quiz) {
        this.quizzes.add(quiz);
        incrementUsageCount();
    }
    
    /**
     * 퀴즈 제거
     * 
     * @param quiz 제거할 퀴즈
     */
    public void removeQuiz(Quiz quiz) {
        if (this.quizzes.remove(quiz)) {
            decrementUsageCount();
        }
    }
    
    /**
     * 태그 활성화/비활성화
     * 
     * @param active 활성화 상태
     */
    public void setActive(boolean active) {
        this.active = active;
        
        // 비활성화 시 모든 자식 태그도 비활성화
        if (!active) {
            children.forEach(child -> child.setActive(false));
        }
    }
    
    /**
     * 루트 태그인지 확인
     * 
     * @return 루트 태그 여부
     */
    public boolean isRoot() {
        return parent == null && level == 0;
    }
    
    /**
     * 리프 태그인지 확인 (자식이 없는 태그)
     * 
     * @return 리프 태그 여부
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    /**
     * 최대 깊이 태그인지 확인
     * 
     * @return 최대 깊이 태그 여부
     */
    public boolean isMaxLevel() {
        return level >= MAX_HIERARCHY_LEVEL - 1;
    }
    
    /**
     * 모든 조상 태그 조회 (루트까지)
     * 
     * @return 조상 태그 스트림 (부모 -> 조부모 -> ... -> 루트 순서)
     */
    public Stream<Tag> getAncestors() {
        return Stream.iterate(this.parent, parent -> parent != null, Tag::getParent);
    }
    
    /**
     * 모든 후손 태그 조회 (재귀적)
     * 
     * @return 후손 태그 스트림
     */
    public Stream<Tag> getDescendants() {
        return children.stream()
                .flatMap(child -> Stream.concat(
                    Stream.of(child),
                    child.getDescendants()
                ));
    }
    
    /**
     * 전체 경로 문자열 생성 (루트 -> ... -> 현재 태그)
     * 
     * @return 경로 문자열 (예: "프로그래밍 > Java > Spring Boot")
     */
    public String getFullPath() {
        List<String> pathComponents = new ArrayList<>();
        
        // 현재 태그부터 루트까지 역순으로 수집
        Tag current = this;
        while (current != null) {
            pathComponents.add(0, current.getName());
            current = current.getParent();
        }
        
        return String.join(" > ", pathComponents);
    }
    
    /**
     * 사용 횟수 증가
     */
    private void incrementUsageCount() {
        this.usageCount++;
    }
    
    /**
     * 사용 횟수 감소
     */
    private void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
    
    /**
     * 태그 이름 유효성 검증
     * 
     * @param name 태그 이름
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("태그 이름은 필수입니다.");
        }
        
        if (name.length() > 50) {
            throw new IllegalArgumentException("태그 이름은 50자를 초과할 수 없습니다.");
        }
    }
    
    /**
     * 계층구조 깊이 유효성 검증
     * 
     * @param parent 부모 태그
     */
    private void validateHierarchyLevel(Tag parent) {
        if (parent != null && parent.getLevel() >= MAX_HIERARCHY_LEVEL - 1) {
            throw new IllegalArgumentException(
                String.format("태그 계층은 최대 %d단계까지만 허용됩니다.", MAX_HIERARCHY_LEVEL)
            );
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        return id != null && id.equals(tag.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Tag{id=%d, name='%s', level=%d, path='%s'}", 
                           id, name, level, getFullPath());
    }
} 