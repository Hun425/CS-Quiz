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
import java.util.ArrayList;
import java.util.List;

/**
 * 문제 도메인 모델
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "questions", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    /**
     * 문제 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 연관된 퀴즈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    /**
     * 문제 내용
     */
    @Column(nullable = false, length = 500)
    private String content;
    
    /**
     * 문제 유형
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * 정답 설명
     */
    @Column(length = 1000)
    private String explanation;
    
    /**
     * 배점
     */
    @Column(nullable = false)
    private Integer points;
    
    /**
     * 보기 목록
     */
    @ElementCollection
    @CollectionTable(
        name = "question_options",
        schema = "quiz_schema",
        joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "option_text", nullable = false)
    private List<String> options = new ArrayList<>();
    
    /**
     * 정답
     */
    @Column(nullable = false)
    private String answer;
    
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
     * 문제 순서
     */
    @Column(name = "question_order")
    private Integer order;
    
    /**
     * 코드 스니펫
     */
    @Column(name = "code_snippet", length = 2000)
    private String codeSnippet;
    
    /**
     * 연관 퀴즈 설정
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    
    /**
     * 문제 유형 반환
     */
    public String getQuestionType() {
        return this.type;
    }
    
    /**
     * 정답 반환
     */
    public String getCorrectAnswer() {
        return this.answer;
    }
    
    /**
     * 보기 추가
     */
    public void addOption(String option) {
        if (this.options == null) {
            this.options = new ArrayList<>();
        }
        this.options.add(option);
    }
    
    /**
     * 문제 복사
     */
    public Question copy() {
        Question copy = Question.builder()
                .content(this.content)
                .type(this.type)
                .explanation(this.explanation)
                .points(this.points)
                .answer(this.answer)
                .order(this.order)
                .codeSnippet(this.codeSnippet)
                .build();
                
        // 보기 복사
        copy.getOptions().addAll(this.options);
        
        return copy;
    }
    
    /**
     * 빌더 패턴용 questionType 메서드 (type 필드와 매핑)
     * 
     * @param type 문제 유형
     * @return Question.QuestionBuilder
     */
    public static Question.QuestionBuilder questionType(String type) {
        return Question.builder().type(type);
    }
} 