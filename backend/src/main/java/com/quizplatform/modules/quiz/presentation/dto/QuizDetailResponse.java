package com.quizplatform.modules.quiz.presentation.dto;

import com.quizplatform.modules.quiz.domain.entity.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.entity.Quiz;
import com.quizplatform.modules.quiz.domain.entity.QuizStatistics;
import com.quizplatform.modules.quiz.domain.entity.QuizType;
import com.quizplatform.modules.user.presentation.dto.UserSummaryResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
// Quiz 관련 응답 DTO들
@Getter
@Builder
public class QuizDetailResponse {
    private Long id;
    private String title;
    private String description;
    private QuizType quizType;
    private DifficultyLevel difficultyLevel;
    private Integer timeLimit;
    private int questionCount;
    private List<TagResponse> tags;
    private UserSummaryResponse creator;
    private QuizStatistics statistics;
    private LocalDateTime createdAt;

    public static QuizDetailResponse from(Quiz quiz) {
        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .quizType(quiz.getQuizType())
                .difficultyLevel(quiz.getDifficultyLevel())
                .timeLimit(quiz.getTimeLimit())
                .questionCount(quiz.getQuestions().size())
                .tags(quiz.getTags().stream()
                        .map(TagResponse::from)
                        .collect(Collectors.toList()))
                .creator(UserSummaryResponse.from(quiz.getCreator()))
                .statistics(QuizStatistics.from(quiz))
                .createdAt(quiz.getCreatedAt())
                .build();
    }
}