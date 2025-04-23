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
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 시도 엔티티 클래스
 */
@Entity
@Table(name = "quiz_attempts", schema = "quiz_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt {
    
    /**
     * 퀴즈 시도 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 퀴즈 ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 사용자 엔티티 (Quiz 모듈에는 User 참조 정보만 저장)
     */
    @Transient
    private UserInfo user;
    
    /**
     * 문제 답변 목록
     */
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAttempt> questionAttempts = new ArrayList<>();
    
    /**
     * 획득 점수
     */
    @Column(nullable = false)
    private int score;
    
    /**
     * 총 문제 수
     */
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;
    
    /**
     * 통과 여부
     */
    @Column(nullable = false)
    private boolean passed;
    
    /**
     * 완료 여부
     */
    @Column(name = "is_completed", nullable = false)
    private boolean completed;
    
    /**
     * 시작 시간
     */
    @Column(name = "started_at", nullable = false)
    private ZonedDateTime startedAt;
    
    /**
     * 완료 시간
     */
    @Column(name = "completed_at")
    private ZonedDateTime completedAt;
    
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
     * 퀴즈 시도 생성자
     * 
     * @param quiz 퀴즈
     * @param userId 사용자 ID
     */
    @Builder
    public QuizAttempt(Quiz quiz, Long userId) {
        this.quiz = quiz;
        this.userId = userId;
        this.score = 0;
        this.totalQuestions = quiz.getQuestions().size();
        this.passed = false;
        this.completed = false;
        this.startedAt = ZonedDateTime.now();
    }
    
    /**
     * 문제 답변 추가
     * 
     * @param questionAttempt 문제 답변
     */
    public void addQuestionAttempt(QuestionAttempt questionAttempt) {
        questionAttempts.add(questionAttempt);
        questionAttempt.setQuizAttempt(this);
    }
    
    /**
     * 퀴즈 완료 처리 및 점수 계산
     * 
     * @return 통과 여부
     */
    public boolean complete() {
        if (this.completed) {
            return this.passed;
        }
        
        this.completedAt = ZonedDateTime.now();
        this.completed = true;
        
        // 점수 계산
        int totalScore = 0;
        int maxScore = 0;
        
        for (QuestionAttempt attempt : questionAttempts) {
            if (attempt.isCorrect()) {
                totalScore += attempt.getQuestion().getPoints();
            }
            maxScore += attempt.getQuestion().getPoints();
        }
        
        this.score = maxScore > 0 ? totalScore : 0;
        
        // 통과 여부 계산
        int scorePercentage = maxScore > 0 ? (totalScore * 100) / maxScore : 0;
        this.passed = scorePercentage >= quiz.getPassingScore();
        
        return this.passed;
    }
    
    /**
     * 남은 미답변 문제 수 반환
     * 
     * @return 미답변 문제 수
     */
    public int getRemainingQuestions() {
        return totalQuestions - questionAttempts.size();
    }
    
    /**
     * 사용자 정보 설정 (UserInfo 객체로부터)
     * 
     * @param userInfo 사용자 정보
     */
    public void setUser(UserInfo userInfo) {
        this.user = userInfo;
    }
    
    /**
     * UserInfo 내부 클래스 - User 모듈과의 의존성 분리를 위한 Value Object
     */
    @Getter
    public static class UserInfo {
        private final Long id;
        private final String username;
        private final String email;
        
        public UserInfo(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }
    }
} 