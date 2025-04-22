package com.quizplatform.quiz.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 답변 제출 요청 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAnswerRequest {

    @NotNull(message = "퀴즈 시도 ID는 필수 항목입니다")
    private Long quizAttemptId;
    
    @NotNull(message = "문제 ID는 필수 항목입니다")
    private Long questionId;
    
    @NotBlank(message = "사용자 답변은 필수 항목입니다")
    private String userAnswer;  // MULTIPLE_CHOICE: 콤마로 구분된 ID 목록, TRUE_FALSE: "true" 또는 "false", TEXT: 텍스트 답변
}
