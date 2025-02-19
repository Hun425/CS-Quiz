package com.quizplatform.core.domain.question;

import com.quizplatform.core.domain.quiz.QuizAttempt;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "question_attempts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id")
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct")
    private boolean isCorrect;

    @Column(name = "time_taken")
    private Integer timeTaken; // 초 단위

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public QuestionAttempt(QuizAttempt quizAttempt, Question question,
                           String userAnswer, boolean isCorrect, Integer timeTaken) {
        this.quizAttempt = quizAttempt;
        this.question = question;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
        this.timeTaken = timeTaken;
    }
}