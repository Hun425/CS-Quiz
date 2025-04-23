package com.quizplatform.quiz.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 태그 도메인 모델
 */
@Entity
@Table(name = "tags", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * 태그 설명
     */
    @Column(length = 255)
    private String description;
    
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
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 태그 정보 업데이트
     */
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * 퀴즈 추가
     */
    public void addQuiz(Quiz quiz) {
        this.quizzes.add(quiz);
    }
    
    /**
     * 퀴즈 제거
     */
    public void removeQuiz(Quiz quiz) {
        this.quizzes.remove(quiz);
    }
} 