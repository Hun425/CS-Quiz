package com.quizplatform.modules.quiz.dto;


import com.quizplatform.modules.quiz.domain.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.Quiz;
import com.quizplatform.modules.quiz.domain.QuizType;
import com.quizplatform.modules.tag.dto.TagResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class QuizSummaryResponse {
    private Long id;
    private String title;
    private QuizType quizType;
    private DifficultyLevel difficultyLevel;
    private int questionCount;
    private int attemptCount;
    private double avgScore;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;

    public static QuizSummaryResponse from(Quiz quiz) {
        return QuizSummaryResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .quizType(quiz.getQuizType())
                .difficultyLevel(quiz.getDifficultyLevel())
                .questionCount(quiz.getQuestions().size())
                .attemptCount(quiz.getAttemptCount())
                .avgScore(quiz.getAvgScore())
                .tags(quiz.getTags().stream()
                        .map(TagResponse::from)
                        .collect(Collectors.toList()))
                .createdAt(quiz.getCreatedAt())
                .build();
    }
}