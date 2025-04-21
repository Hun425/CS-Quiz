package com.quizplatform.modules.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 답변 생성/수정 요청 DTO
 * <p>
 * 답변 생성 및 수정 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    private String content;
    private Boolean isCorrect;
}