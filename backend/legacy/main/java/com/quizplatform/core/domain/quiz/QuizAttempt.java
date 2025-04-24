package com.quizplatform.core.domain.quiz;


import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private Integer score;

    @Column(name = "is_completed")
    private boolean isCompleted = false;

    @Column(name = "time_taken")
    private Integer timeTaken; // 초 단위

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type")
    private QuizType quizType = QuizType.REGULAR; // 기본값은 일반 퀴즈

    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAttempt> questionAttempts = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public QuizAttempt(User user, Quiz quiz) {
        this.user = user;
        this.quiz = quiz;
        this.startTime = LocalDateTime.now();
    }

    // 퀴즈 완료 처리
    public void complete() {
        this.endTime = LocalDateTime.now();
        if (this.timeTaken == null) {
            this.timeTaken = calculateTimeTaken();
        }
        this.score = calculateScore();
        this.isCompleted = true;
    }

    // 문제 답변 추가
    public QuestionAttempt addQuestionAttempt(Question question, String userAnswer) {
        QuestionAttempt questionAttempt = QuestionAttempt.builder()
                .quizAttempt(this)
                .question(question)
                .userAnswer(userAnswer)
                .isCorrect(question.isCorrectAnswer(userAnswer))
                .timeTaken(calculateTimeTakenSinceLastAttempt())
                .build();

        this.questionAttempts.add(questionAttempt);
        return questionAttempt;
    }

    // 점수 계산
    private int calculateScore() {
        int totalPoints = quiz.getQuestions().stream()
                .mapToInt(Question::getPoints)
                .sum();

        int earnedPoints = questionAttempts.stream()
                .filter(QuestionAttempt::isCorrect)
                .mapToInt(attempt -> attempt.getQuestion().getPoints())
                .sum();

        return totalPoints == 0 ? 0 : (int) ((double) earnedPoints / totalPoints * 100);
    }

    // 총 소요 시간 계산 (초 단위)
    private int calculateTimeTaken() {
        return (int) java.time.Duration.between(startTime, endTime).getSeconds();
    }

    private int calculateTimeTakenSinceLastAttempt() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAttemptTime;

        if (questionAttempts.isEmpty()) {
            lastAttemptTime = startTime;
        } else {
            QuestionAttempt lastAttempt = questionAttempts.get(questionAttempts.size() - 1);
            // createdAt이 null인 경우 startTime으로 대체
            lastAttemptTime = lastAttempt.getCreatedAt() != null ?
                    lastAttempt.getCreatedAt() :
                    startTime;
        }

        // 안전 장치: lastAttemptTime이 여전히 null인 경우
        if (lastAttemptTime == null) {
            return 0; // 유효한 시간이 없는 경우 기본값 0 반환
        }

        return (int) java.time.Duration.between(lastAttemptTime, now).getSeconds();
    }

    // 제한 시간 초과 여부 확인
    public boolean isTimeExpired() {
        if (quiz.getTimeLimit() == null) return false;
        LocalDateTime deadline = startTime.plusMinutes(quiz.getTimeLimit());
        return LocalDateTime.now().isAfter(deadline);
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }
}