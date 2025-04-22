package com.quizplatform.quiz.adapter.in.web.dto;

import com.quizplatform.quiz.domain.model.QuizAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 시도 결과 응답 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResponse {

    private Long id;
    private Long userId;
    private QuizResponse quiz;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer score;
    private Boolean passed;
    private QuizAttempt.AttemptStatus status;
    private List<QuestionAttemptResponse> questionAttempts = new ArrayList<>();
    
    // 추가 통계 정보
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer maxPossibleScore;
    private Double percentageScore;
    private Long timeSpentSeconds;
}
