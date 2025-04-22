package com.quizplatform.quiz.application.port.in.command;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 퀴즈 수정 명령
 */
@Getter
public class UpdateQuizCommand {
    private final Long creatorId; // 권한 확인용
    private final String title;
    private final String description;
    private final DifficultyLevel difficultyLevel;
    private final Integer timeLimit;
    private final Set<Long> tagIds;
    private final List<CreateQuestionCommand> questions;

    @Builder
    public UpdateQuizCommand(
            Long creatorId,
            String title,
            String description,
            DifficultyLevel difficultyLevel,
            Integer timeLimit,
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
        this.difficultyLevel = difficultyLevel;
        this.timeLimit = timeLimit;
        this.tagIds = tagIds != null ? tagIds : new HashSet<>();
        this.questions = questions != null ? questions : new ArrayList<>();
    }
}