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
 * 문제 시도 엔티티
 * 
 * <p>사용자가 개별 문제에 대한 답변을 시도한 정보를 저장합니다.
 * 퀴즈 시도(QuizAttempt)에 포함됩니다.</p>
 */
@Entity
@Table(name = "question_attempts", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAttempt {
    
    /**
     * 문제 시도 ID
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
    @ManyToOne(fetch = FetchType.EAGER)
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
     * 응답 시간
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
     * 생성자
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

    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuizAttempt getQuizAttempt() {
        return quizAttempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public ZonedDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(ZonedDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
} 