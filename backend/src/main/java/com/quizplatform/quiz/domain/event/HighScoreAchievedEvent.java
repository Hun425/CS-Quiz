package com.quizplatform.quiz.domain.event;

import lombok.Getter;

/**
 * 높은 점수 달성 이벤트
 */
@Getter
public class HighScoreAchievedEvent extends DomainEvent {
    private final Long quizId;
    private final Long userId;
    private final Integer score;
    private final Integer previousBestScore;
    private final boolean isGlobalTopScore;

    public HighScoreAchievedEvent(
            Long quizId,
            Long userId,
            Integer score,
            Integer previousBestScore,
            boolean isGlobalTopScore
    ) {
        super("HIGH_SCORE_ACHIEVED");
        this.quizId = quizId;
        this.userId = userId;
        this.score = score;
        this.previousBestScore = previousBestScore;
        this.isGlobalTopScore = isGlobalTopScore;
    }
}