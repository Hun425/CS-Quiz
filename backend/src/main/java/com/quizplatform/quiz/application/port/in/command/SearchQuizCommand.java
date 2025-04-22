package com.quizplatform.quiz.application.port.in.command;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * 퀴즈 검색 명령
 */
@Getter
public class SearchQuizCommand {
    private final String keyword;
    private final Set<Long> tagIds;
    private final Set<DifficultyLevel> difficultyLevels;
    private final Set<QuizType> quizTypes;
    private final Long creatorId;
    private final boolean publicOnly;
    private final int limit;
    private final int offset;

    @Builder
    public SearchQuizCommand(
            String keyword,
            Set<Long> tagIds,
            Set<DifficultyLevel> difficultyLevels,
            Set<QuizType> quizTypes,
            Long creatorId,
            Boolean publicOnly,
            Integer limit,
            Integer offset
    ) {
        this.keyword = keyword;
        this.tagIds = tagIds != null ? tagIds : new HashSet<>();
        this.difficultyLevels = difficultyLevels != null ? difficultyLevels : new HashSet<>();
        this.quizTypes = quizTypes != null ? quizTypes : new HashSet<>();
        this.creatorId = creatorId;
        this.publicOnly = publicOnly != null ? publicOnly : true;
        this.limit = limit != null ? limit : 10;
        this.offset = offset != null ? offset : 0;
    }
}