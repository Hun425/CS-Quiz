package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.service.quiz.QuizSearchCondition;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class QuizSearchRequest {
    private String title;
    private DifficultyLevel difficultyLevel;
    private QuizType quizType;
    private List<Long> tagIds;
    private Integer minQuestions;
    private Integer maxQuestions;
    private String orderBy;

    // 2. QuizSearchRequest.java의 toCondition 메서드 수정
    public QuizSearchCondition toCondition() {
        QuizSearchCondition condition = QuizSearchCondition.builder()
                .title(title)
                .difficultyLevel(difficultyLevel)
                .quizType(quizType)
                .tagIds(tagIds != null ? tagIds : Collections.emptyList())
                .minQuestions(minQuestions)
                .maxQuestions(maxQuestions)
                .orderBy(orderBy)
                .build();

        // 검색 조건 유효성 검사 호출
        condition.validate();

        return condition;
    }

    public void setTagIds(String tagIdsStr) {
        if (tagIdsStr != null && !tagIdsStr.isEmpty()) {
            this.tagIds = Arrays.stream(tagIdsStr.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
    }
}