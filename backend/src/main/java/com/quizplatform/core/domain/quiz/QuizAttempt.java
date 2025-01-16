package com.quizplatform.core.domain.quiz;

import com.quizplatform.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    private Integer score;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "time_taken")
    private Integer timeTaken; // 초 단위로 저장

    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAttempt> questionAttempts = new ArrayList<>();

    @Column(name = "last_answered_question_index")
    private Integer lastAnsweredQuestionIndex;

    @Column(name = "remaining_time")
    private Integer remainingTime; // 초 단위로 저장

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        if (lastAnsweredQuestionIndex == null) {
            lastAnsweredQuestionIndex = 0;
        }
        isCompleted = false;
    }

    @Builder
    public QuizAttempt(User user, Quiz quiz) {
        this.user = user;
        this.quiz = quiz;
        this.startTime = ZonedDateTime.now();
        this.lastAnsweredQuestionIndex = 0;
        this.isCompleted = false;
        if (quiz.getTimeLimit() != null) {
            this.remainingTime = quiz.getTimeLimit() * 60; // 분을 초로 변환
        }
    }

    public void addQuestionAttempt(QuestionAttempt questionAttempt) {
        questionAttempts.add(questionAttempt);
        questionAttempt.setQuizAttempt(this);
        lastAnsweredQuestionIndex = questionAttempts.size() - 1;
        updateRemainingTime();
    }

    public void completeAttempt() {
        if (!isCompleted) {
            this.endTime = ZonedDateTime.now();
            this.timeTaken = calculateTimeTaken();
            this.score = calculateScore();
            this.isCompleted = true;

            // 퀴즈 통계 업데이트
            quiz.addNewAttempt(this.score);
        }
    }

    public void updateRemainingTime() {
        if (remainingTime != null && !isCompleted) {
            Duration timePassed = Duration.between(startTime, ZonedDateTime.now());
            remainingTime = quiz.getTimeLimit() * 60 - (int) timePassed.getSeconds();
            if (remainingTime <= 0) {
                completeAttempt();
            }
        }
    }

    private int calculateTimeTaken() {
        if (startTime != null && endTime != null) {
            return (int) Duration.between(startTime, endTime).getSeconds();
        }
        return 0;
    }

    private int calculateScore() {
        return questionAttempts.stream()
                .filter(QuestionAttempt::isCorrect)
                .mapToInt(qa -> qa.getQuestion().getPoints())
                .sum();
    }

    public boolean canResume() {
        if (isCompleted) {
            return false;
        }
        if (quiz.getTimeLimit() != null && remainingTime != null) {
            return remainingTime > 0;
        }
        return true;
    }

    public double getProgressPercentage() {
        if (quiz.getQuestionCount() == 0) {
            return 0.0;
        }
        return (double) questionAttempts.size() / quiz.getQuestionCount() * 100;
    }

    public QuestionAttempt getCurrentQuestionAttempt() {
        if (lastAnsweredQuestionIndex < questionAttempts.size()) {
            return questionAttempts.get(lastAnsweredQuestionIndex);
        }
        return null;
    }
}