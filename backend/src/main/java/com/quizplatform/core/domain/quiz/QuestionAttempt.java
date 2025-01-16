package com.quizplatform.core.domain.quiz;

import com.quizplatform.core.domain.quiz.Question;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "question_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "time_taken")
    private Integer timeTaken; // 초 단위

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }

    @Builder
    public QuestionAttempt(QuizAttempt quizAttempt, Question question,
                           String userAnswer, Integer timeTaken) {
        this.quizAttempt = quizAttempt;
        this.question = question;
        this.userAnswer = userAnswer;
        this.timeTaken = timeTaken;
        this.isCorrect = question.checkAnswer(userAnswer);
    }

    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }
}