package application.port.in.command;

import domain.model.DifficultyLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 퀴즈 수정을 위한 데이터를 담는 커맨드 객체
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class UpdateQuizCommand {
    private final Long quizId; // 수정할 퀴즈 ID
    private final Long creatorId; // 권한 확인용
    private final String title;
    private final String description;
    private final DifficultyLevel difficultyLevel;
    private final Integer timeLimit;
    private final Set<Long> tagIds;
    private final List<CreateQuestionCommand> questions;

    @Builder
    public UpdateQuizCommand(
            Long quizId,
            Long creatorId,
            String title,
            String description,
            DifficultyLevel difficultyLevel,
            Integer timeLimit,
            Set<Long> tagIds,
            List<CreateQuestionCommand> questions
    ) {
        // 유효성 검사
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID must not be null");
        }
        if (creatorId == null) {
            throw new IllegalArgumentException("Creator ID must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }

        this.quizId = quizId;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
        this.tagIds = tagIds != null ? tagIds : new HashSet<>();
        this.questions = questions != null ? questions : new ArrayList<>();
    }
}