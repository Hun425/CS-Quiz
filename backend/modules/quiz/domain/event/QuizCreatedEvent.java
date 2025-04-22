package domain.event;

import domain.model.DifficultyLevel;
import domain.model.Quiz;
import domain.model.QuizType;
import lombok.Getter;
import domain.event.DomainEvent;

import java.util.Set;

/**
 * 퀴즈 생성 이벤트
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class QuizCreatedEvent extends DomainEvent {
    private final Long quizId;
    private final Long creatorId;
    private final String title;
    private final QuizType quizType;
    private final DifficultyLevel difficultyLevel;
    private final Set<Long> tagIds;
    private final int questionCount;

    public QuizCreatedEvent(Quiz quiz) {
        super("QUIZ_CREATED");
        this.quizId = quiz.getId();
        this.creatorId = quiz.getCreatorId();
        this.title = quiz.getTitle();
        this.quizType = quiz.getQuizType();
        this.difficultyLevel = quiz.getDifficultyLevel();
        this.tagIds = quiz.getTagIds();
        this.questionCount = quiz.getQuestionCount();
    }
}