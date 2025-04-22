package domain.event;

import lombok.Getter;
import domain.event.DomainEvent;

/**
 * 높은 점수 달성 이벤트
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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