package com.quizplatform.quiz.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 문제 시도 응답 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAttemptResponse {

    private Long id;
    private QuestionResponse question;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private LocalDateTime answerTime;
}
