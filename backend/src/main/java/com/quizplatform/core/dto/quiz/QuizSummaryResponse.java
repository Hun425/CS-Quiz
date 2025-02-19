package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.dto.tag.TagResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class QuizSummaryResponse {
    private UUID id;
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