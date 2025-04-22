package application.port.in.command;

import domain.model.DifficultyLevel;
import domain.model.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 새로운 퀴즈 생성을 위한 데이터를 담는 커맨드 객체
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class CreateQuizCommand {
    private final Long creatorId;
    private final String title;
    private final String description;
    private final QuizType quizType;
    private final DifficultyLevel difficultyLevel;
    private final Integer timeLimit;
    private final boolean isPublic;
    private final Set<Long> tagIds;
    private final List<CreateQuestionCommand> questions;

    @Builder
    public CreateQuizCommand(
            Long creatorId,
            String title,
            String description,
            QuizType quizType,
            DifficultyLevel difficultyLevel,
            Integer timeLimit,
            Boolean isPublic,
            Set<Long> tagIds,
            List<CreateQuestionCommand> questions
    ) {
        // 유효성 검사
        if (creatorId == null) {
            throw new IllegalArgumentException("Creator ID must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }

        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.quizType = quizType != null ? quizType : QuizType.REGULAR;
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.BEGINNER;
        this.timeLimit = timeLimit;
        this.isPublic = isPublic != null ? isPublic : true;
        this.tagIds = tagIds != null ? tagIds : new HashSet<>();
        this.questions = questions != null ? questions : new ArrayList<>();
    }
}