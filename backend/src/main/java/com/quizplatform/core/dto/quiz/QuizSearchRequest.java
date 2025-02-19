package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.service.quiz.QuizSearchCondition;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class QuizSearchRequest {
    private String title;
    private DifficultyLevel difficultyLevel;
    private QuizType quizType;
    private List<UUID> tagIds;
    private Integer minQuestions;
    private Integer maxQuestions;
    private String orderBy;

    public QuizSearchCondition toCondition() {
        return QuizSearchCondition.builder()
                .title(title)
                .difficultyLevel(difficultyLevel)
                .quizType(quizType)
                .tagIds(tagIds != null ? tagIds : Collections.emptyList())
                .minQuestions(minQuestions)
                .maxQuestions(maxQuestions)
                .orderBy(orderBy)
                .build();
    }
}