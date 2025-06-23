package com.quizplatform.core.dto.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizStatistics;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.dto.user.UserSummaryResponse;
import com.quizplatform.core.dto.tag.TagResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
// Quiz 관련 응답 DTO들
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDetailResponse {
    private Long id;
    private String title;
    private String description;
    private QuizType quizType;
    private DifficultyLevel difficultyLevel;
    private int questionCount;
    private Integer timeLimit;
    private Long creatorId;
    private String creatorUsername;
    private String creatorProfileImage;
    private int viewCount;
    private int attemptCount;
    private double avgScore;
    private LocalDateTime createdAt;
    private Boolean isPublic;
    private List<TagResponse> tags;
    private UserSummaryResponse creator;
    private QuizStatistics statistics;

    // statistics가 null이면 빈 배열로 반환
    @JsonGetter("statistics")
    public Object getStatisticsSafe() {
        return statistics != null ? statistics : Collections.emptyList();
    }

    public static QuizDetailResponse from(Quiz quiz) {
        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .quizType(quiz.getQuizType())
                .difficultyLevel(quiz.getDifficultyLevel())
                .questionCount(quiz.getQuestions().size())
                .timeLimit(quiz.getTimeLimit())
                .creatorId(quiz.getCreator().getId())
                .creatorUsername(quiz.getCreator().getUsername())
                .creatorProfileImage(quiz.getCreator().getProfileImage())
                .viewCount(quiz.getViewCount())
                .attemptCount(quiz.getAttemptCount())
                .avgScore(quiz.getAvgScore())
                .createdAt(quiz.getCreatedAt())
                .isPublic(quiz.isPublic())
                .tags(quiz.getTags().stream()
                        .map(TagResponse::from)
                        .collect(Collectors.toList()))
                .creator(UserSummaryResponse.from(quiz.getCreator()))
                .statistics(QuizStatistics.from(quiz))
                .build();
    }
}