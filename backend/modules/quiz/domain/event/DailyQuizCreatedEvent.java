package domain.event;

import lombok.Getter;
import domain.event.DomainEvent;

import java.util.Set;

/**
 * 데일리 퀴즈 생성 이벤트
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class DailyQuizCreatedEvent extends DomainEvent {
    private final Long quizId;
    private final String title;
    private final Set<Long> tagIds;
    private final int questionCount;

    public DailyQuizCreatedEvent(Long quizId, String title, Set<Long> tagIds, int questionCount) {
        super("DAILY_QUIZ_CREATED");
        this.quizId = quizId;
        this.title = title;
        this.tagIds = tagIds;
        this.questionCount = questionCount;
    }
}