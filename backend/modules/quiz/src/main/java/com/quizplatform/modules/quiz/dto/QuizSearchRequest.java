package com.quizplatform.modules.quiz.dto;


import com.quizplatform.modules.quiz.domain.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.QuizType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class QuizSearchRequest {
    private String title;
    private DifficultyLevel difficultyLevel;
    private QuizType quizType;
    private List<Long> tagIds;
    private Integer minQuestions;
    private Integer maxQuestions;
    private String orderBy;

    // QuizSearchRequest.java의 toCondition 메서드
    public QuizSubmitRequest.QuizSearchCondition toCondition() {
        QuizSubmitRequest.QuizSearchCondition condition = QuizSubmitRequest.QuizSearchCondition.builder()
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

        // 디버깅 로그 추가
        if (tagIds != null && !tagIds.isEmpty()) {
            log.debug("태그 검색 요청: tagIds={}", tagIds);
        }

        return condition;
    }

    // 태그 ID를 문자열로 받아서 Long 리스트로 변환
    public void setTagIds(String tagIdsStr) {
        if (tagIdsStr != null && !tagIdsStr.isEmpty()) {
            try {
                this.tagIds = Arrays.stream(tagIdsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                log.debug("문자열에서 변환된 태그 ID 목록: {}", this.tagIds);
            } catch (NumberFormatException e) {
                log.error("태그 ID 변환 중 오류 발생: {}", e.getMessage());
                this.tagIds = new ArrayList<>(); // 오류 발생 시 빈 리스트로 초기화
            }
        } else {
            this.tagIds = new ArrayList<>();
        }
    }

    // 문자열로 받은 난이도 값을 enum으로 변환
    public void setDifficultyLevel(String difficultyLevelStr) {
        if (difficultyLevelStr != null && !difficultyLevelStr.isEmpty()) {
            try {
                this.difficultyLevel = DifficultyLevel.valueOf(difficultyLevelStr.toUpperCase());
                log.debug("난이도 설정: {}", this.difficultyLevel);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 난이도 값: {}", difficultyLevelStr);
                // 잘못된 값은 무시
            }
        }
    }

    // 문자열로 받은 퀴즈 타입 값을 enum으로 변환
    public void setQuizType(String quizTypeStr) {
        if (quizTypeStr != null && !quizTypeStr.isEmpty()) {
            try {
                this.quizType = QuizType.valueOf(quizTypeStr.toUpperCase());
                log.debug("퀴즈 타입 설정: {}", this.quizType);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 퀴즈 타입 값: {}", quizTypeStr);
                // 잘못된 값은 무시
            }
        }
    }

    @Override
    public String toString() {
        return "QuizSearchRequest{" +
                "title='" + title + '\'' +
                ", difficultyLevel=" + difficultyLevel +
                ", quizType=" + quizType +
                ", tagIds=" + tagIds +
                ", minQuestions=" + minQuestions +
                ", maxQuestions=" + maxQuestions +
                ", orderBy='" + orderBy + '\'' +
                '}';
    }
}