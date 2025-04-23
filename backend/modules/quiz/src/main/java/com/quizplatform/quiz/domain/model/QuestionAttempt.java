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

/**
 * 문제 시도 도메인 모델
 */
@Entity
@Table(name = "question_attempts", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAttempt {
    /**
     * 문제 시도 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 연관된 퀴즈 시도
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;
    
    /**
     * 문제 ID
     */
    @Column(nullable = false)
    private Long questionId;
    
    /**
     * 사용자 답변
     */
    @Column(nullable = false)
    private String userAnswer;
    
    /**
     * 정답 여부
     */
    @Column(nullable = false)
    private boolean correct;
    
    /**
     * 시도 시간
     */
    @Column(nullable = false)
    private LocalDateTime attemptTime;
    
    /**
     * 소요 시간 (초)
     */
    @Column(nullable = false)
    private int timeSpentSeconds;
    
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
     * 퀴즈 시도 설정
     */
    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }
    
    /**
     * 정답 확인 및 설정
     * 
     * @param correctAnswer 정답
     * @return 정답 여부
     */
    public boolean checkAnswer(String correctAnswer) {
        this.correct = this.userAnswer.equalsIgnoreCase(correctAnswer.trim());
        return this.correct;
    }
} 