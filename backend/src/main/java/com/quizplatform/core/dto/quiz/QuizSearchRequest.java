package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.service.quiz.QuizSearchCondition;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter // Setter 어노테이션 추가
public class QuizSearchRequest {
    private String title;
    private DifficultyLevel difficultyLevel;
    private QuizType quizType;
    private List<Long> tagIds;
    private Integer minQuestions;
    private Integer maxQuestions;
    private String orderBy;

    // QuizSearchRequest.java의 toCondition 메서드
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

    // 태그 ID를 문자열로 받아서 Long 리스트로 변환
    public void setTagIds(String tagIdsStr) {
        if (tagIdsStr != null && !tagIdsStr.isEmpty()) {
            this.tagIds = Arrays.stream(tagIdsStr.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
    }

    // 문자열로 받은 난이도 값을 enum으로 변환
    public void setDifficultyLevel(String difficultyLevelStr) {
        if (difficultyLevelStr != null && !difficultyLevelStr.isEmpty()) {
            try {
                this.difficultyLevel = DifficultyLevel.valueOf(difficultyLevelStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 값은 무시
            }
        }
    }

    // 문자열로 받은 퀴즈 타입 값을 enum으로 변환
    public void setQuizType(String quizTypeStr) {
        if (quizTypeStr != null && !quizTypeStr.isEmpty()) {
            try {
                this.quizType = QuizType.valueOf(quizTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 값은 무시
            }
        }
    }
}