package com.quizplatform.quiz.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

/**
 * 문제 답변 엔티티 클래스
 */
@Entity
@Table(name = "question_attempts", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAttempt {
    
    /**
     * 문제 답변 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 연결된 퀴즈 시도
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;
    
    /**
     * 연결된 문제
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    /**
     * 사용자 답변
     */
    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;
    
    /**
     * 정답 여부
     */
    @Column(nullable = false)
    private boolean correct;
    
    /**
     * 답변 제출 시간
     */
    @Column(name = "answered_at")
    private ZonedDateTime answeredAt;
    
    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    /**
     * 문제 답변 생성자
     * 
     * @param question 문제
     * @param userAnswer 사용자 답변
     */
    @Builder
    public QuestionAttempt(Question question, String userAnswer) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.correct = question.checkAnswer(userAnswer);
        this.answeredAt = ZonedDateTime.now();
    }
    
    /**
     * 퀴즈 시도 설정
     * 
     * @param quizAttempt 연결할 퀴즈 시도
     */
    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }
    
    /**
     * 답변 업데이트
     * 
     * @param userAnswer 새 사용자 답변
     * @return 정답 여부
     */
    public boolean updateAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
        this.correct = question.checkAnswer(userAnswer);
        this.answeredAt = ZonedDateTime.now();
        return this.correct;
    }
} 