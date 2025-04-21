package com.quizplatform.modules.quiz.dto;


import com.quizplatform.modules.quiz.domain.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.Quiz;
import com.quizplatform.modules.quiz.domain.QuizType;
import com.quizplatform.modules.tag.dto.TagResponse;
import com.quizplatform.modules.user.dto.UserSummaryResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
// DTO 클래스들
@Getter
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private QuizType quizType;
    private DifficultyLevel difficultyLevel;
    private Integer timeLimit;
    private int questionCount;
    private List<TagResponse> tags;
    private List<QuestionResponse> questions;
    private UserSummaryResponse creator;
    private LocalDateTime createdAt;
    private Long quizAttemptId; // 새로 추가된 필드

    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
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
                .questions(quiz.getQuestions().stream()
                        .map(QuestionResponse::from)
                        .collect(Collectors.toList()))
                .creator(UserSummaryResponse.from(quiz.getCreator()))
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    // quizAttemptId를 설정하는 메서드 추가
    public QuizResponse withQuizAttemptId(Long quizAttemptId) {
        return QuizResponse.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .quizType(this.quizType)
                .difficultyLevel(this.difficultyLevel)
                .timeLimit(this.timeLimit)
                .questionCount(this.questionCount)
                .tags(this.tags)
                .questions(this.questions)
                .creator(this.creator)
                .createdAt(this.createdAt)
                .quizAttemptId(quizAttemptId)
                .build();
    }
}