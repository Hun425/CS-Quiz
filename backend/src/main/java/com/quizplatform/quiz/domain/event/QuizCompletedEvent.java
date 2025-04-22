package com.quizplatform.quiz.domain.event;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.QuizAttempt;
import com.quizplatform.quiz.domain.model.QuizType;
import lombok.Getter;

/**
 * 퀴즈 완료 이벤트
 */
@Getter
public class QuizCompletedEvent extends DomainEvent {
    private final Long quizId;
    private final Long userId;
    private final Integer score;
    private final Integer timeTaken;
    private final QuizType quizType;
    private final DifficultyLevel difficultyLevel;
    private final int experienceGained;

    public QuizCompletedEvent(
            Long quizId,
            Long userId,
            Integer score,
            Integer timeTaken,
            QuizType quizType,
            DifficultyLevel difficultyLevel
    ) {
        super("QUIZ_COMPLETED");
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.timeTaken = timeTaken;
        this.quizType = quizType;
        this.difficultyLevel = difficultyLevel;
        this.experienceGained = calculateExperience(score, difficultyLevel);
    }

    public QuizCompletedEvent(QuizAttempt quizAttempt, DifficultyLevel difficultyLevel) {
        super("QUIZ_COMPLETED");
        this.quizId = quizAttempt.getQuizId();
        this.userId = quizAttempt.getUserId();
        this.score = quizAttempt.getScore();
        this.timeTaken = quizAttempt.getTimeTaken();
        this.quizType = quizAttempt.getQuizType();
        this.difficultyLevel = difficultyLevel;
        this.experienceGained = calculateExperience(quizAttempt.getScore(), difficultyLevel);
    }

    private int calculateExperience(Integer score, DifficultyLevel difficultyLevel) {
        if (score == null) return 0;
        
        // 기본 경험치에 점수 비율을 곱하여 계산
        double baseExp = difficultyLevel.getBaseExp();
        return (int) (baseExp * score / 100.0);
    }
}