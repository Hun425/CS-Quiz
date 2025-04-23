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
 * 문제 선택지 엔티티
 * 
 * <p>객관식 문제의 선택지 정보를 저장합니다.
 * 선택지 내용, 정답 여부 등을 관리합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "question_options", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionOption {
    
    /**
     * 선택지 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 연결된 문제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 선택지 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    /**
     * 정답 여부
     */
    @Column(name = "is_correct", nullable = false)
    private boolean correct;
    
    /**
     * 선택지 순서
     */
    @Column(name = "option_order", nullable = false)
    private int optionOrder;
    
    /**
     * 설명 (옵션 선택 시 표시할 추가 설명)
     */
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
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
     * 생성자
     */
    @Builder
    public QuestionOption(String content, boolean correct, int optionOrder, String explanation) {
        this.content = content;
        this.correct = correct;
        this.optionOrder = optionOrder;
        this.explanation = explanation;
    }
    
    /**
     * 문제 설정
     * 
     * @param question 연결할 문제
     */
    public void setQuestion(Question question) {
        this.question = question;
    }
    
    /**
     * 선택지 내용 업데이트
     * 
     * @param content 선택지 내용
     * @param correct 정답 여부
     * @param explanation 선택지 설명
     */
    public void update(String content, boolean correct, String explanation) {
        this.content = content;
        this.correct = correct;
        this.explanation = explanation;
    }
} 