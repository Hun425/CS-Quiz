package com.quizplatform.modules.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 질문 생성/수정 요청 DTO
 * <p>
 * 질문 생성 및 수정 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    private String content;
    private String type;
    private Integer points;
    private List<AnswerRequest> answers;
    private String explanation;
}