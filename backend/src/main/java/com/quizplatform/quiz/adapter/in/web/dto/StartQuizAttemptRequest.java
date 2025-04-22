package com.quizplatform.quiz.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 시도 시작 요청 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartQuizAttemptRequest {

    @NotNull(message = "퀴즈 ID는 필수 항목입니다")
    private Long quizId;
    
    @NotNull(message = "사용자 ID는 필수 항목입니다")
    private Long userId;
}
