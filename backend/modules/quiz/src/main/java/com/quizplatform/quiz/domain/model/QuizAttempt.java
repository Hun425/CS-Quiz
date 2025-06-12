package com.quizplatform.quiz.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 퀴즈 시도 도메인 모델
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "quiz_attempts", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {
    /**
     * 퀴즈 시도 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 시도한 퀴즈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    /**
     * 시도한 사용자 ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 시작 시간
     */
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    /**
     * 종료 시간
     */
    private LocalDateTime endTime;
    
    /**
     * 완료 여부
     */
    @Column(nullable = false)
    private boolean completed;
    
    /**
     * 통과 여부
     */
    @Column(nullable = false)
    private boolean passed;
    
    /**
     * 총점
     */
    @Column(nullable = false)
    private int score;
    
    /**
     * 총 문제 수
     */
    @Column(nullable = false)
    private int totalQuestions;
    
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
     * 퀴즈 시도 중 각 문제에 대한 시도 목록
     */
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuestionAttempt> questionAttempts = new ArrayList<>();
    
    /**
     * 퀴즈 시도 시작
     */
    @PrePersist
    public void prePersist() {
        this.startTime = LocalDateTime.now();
        this.completed = false;
        this.passed = false;
        this.score = 0;
        this.totalQuestions = this.quiz.getQuestions().size();
    }
    
    /**
     * 문제 시도 추가
     */
    public void addQuestionAttempt(QuestionAttempt questionAttempt) {
        questionAttempts.add(questionAttempt);
        questionAttempt.setQuizAttempt(this);
    }
    
    /**
     * 퀴즈 시도 완료 처리
     * 
     * @return 통과 여부
     */
    public boolean complete() {
        if (this.completed) {
            return this.passed;
        }
        
        this.endTime = LocalDateTime.now();
        this.completed = true;
        
        // 점수 계산 - 개선된 로직
        this.score = calculateScore();
        
        // 통과 기준 확인
        this.passed = checkPassingScore();
        
        return this.passed;
    }
    
    /**
     * 점수 계산 (백분율 기준)
     * 
     * @return 계산된 점수 (0-100)
     */
    private int calculateScore() {
        if (this.quiz.getQuestions().isEmpty()) {
            return 0;
        }
        
        AtomicInteger totalPoints = new AtomicInteger(0);
        AtomicInteger earnedPoints = new AtomicInteger(0);
        
        this.quiz.getQuestions().forEach(question -> {
            int questionPoints = question.getPoints() != null ? question.getPoints() : 10; // 기본 10점
            totalPoints.addAndGet(questionPoints);
            
            // 해당 문제에 대한 정답 시도 찾기 (람다 적극 활용)
            questionAttempts.stream()
                    .filter(attempt -> attempt.getQuestionId().equals(question.getId()))
                    .filter(QuestionAttempt::isCorrect)
                    .findFirst()
                    .ifPresent(attempt -> earnedPoints.addAndGet(questionPoints));
        });
        
        if (totalPoints.get() == 0) {
            return 0;
        }
        
        // 백분율로 변환 (0-100)
        return Math.round((float) earnedPoints.get() / totalPoints.get() * 100);
    }
    
    /**
     * 통과 여부 확인
     * 
     * @return 통과 여부
     */
    private boolean checkPassingScore() {
        int passingScore = this.quiz.getPassingScore();
        return this.score >= passingScore;
    }
    
    /**
     * 정답 문제 수 반환
     * 
     * @return 정답 문제 수
     */
    public int getCorrectAnswerCount() {
        return (int) questionAttempts.stream()
                .filter(QuestionAttempt::isCorrect)
                .count();
    }
    
    /**
     * 오답 문제 수 반환
     * 
     * @return 오답 문제 수
     */
    public int getIncorrectAnswerCount() {
        return questionAttempts.size() - getCorrectAnswerCount();
    }
    
    /**
     * 정답률 반환 (0.0 - 1.0)
     * 
     * @return 정답률
     */
    public double getAccuracy() {
        if (questionAttempts.isEmpty()) {
            return 0.0;
        }
        return (double) getCorrectAnswerCount() / questionAttempts.size();
    }
    
    /**
     * 퀴즈 시도 소요 시간 계산
     * 
     * @return 소요 시간 (초)
     */
    public long getDurationInSeconds() {
        if (endTime == null) {
            return Duration.between(startTime, LocalDateTime.now()).getSeconds();
        }
        return Duration.between(startTime, endTime).getSeconds();
    }
    
    /**
     * 시도 소요 시간 반환 (초)
     * QuizResultProcessor에서 사용
     */
    public Long getTimeTaken() {
        return getDurationInSeconds();
    }
} 