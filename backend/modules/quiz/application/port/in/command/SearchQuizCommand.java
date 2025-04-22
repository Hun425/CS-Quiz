package application.port.in.command;

import domain.model.DifficultyLevel;
import domain.model.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * 퀴즈 검색 조건을 담는 커맨드 객체
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public class SearchQuizCommand {
    private final String keyword;
    private final Set<Long> tagIds;
    private final Set<DifficultyLevel> difficultyLevels;
    private final Set<QuizType> quizTypes;
    private final Long creatorId;
    private final boolean publicOnly;

    @Builder
    public SearchQuizCommand(
            String keyword,
            Set<Long> tagIds,
            Set<DifficultyLevel> difficultyLevels,
            Set<QuizType> quizTypes,
            Long creatorId,
            Boolean publicOnly
    ) {
        this.keyword = keyword;
        this.tagIds = tagIds != null ? tagIds : new HashSet<>();
        this.difficultyLevels = difficultyLevels != null ? difficultyLevels : new HashSet<>();
        this.quizTypes = quizTypes != null ? quizTypes : new HashSet<>();
        this.creatorId = creatorId;
        this.publicOnly = publicOnly != null ? publicOnly : true;
    }
}