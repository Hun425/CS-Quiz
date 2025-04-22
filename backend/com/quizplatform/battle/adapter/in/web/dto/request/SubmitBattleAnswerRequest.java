package com.quizplatform.battle.adapter.in.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 배틀 답변 제출 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitBattleAnswerRequest {
    
    /**
     * 사용자 ID
     */
    @NotNull(message = "사용자 ID는 필수입니다")
    private UUID userId;
    
    /**
     * 질문 ID
     */
    @NotNull(message = "질문 ID는 필수입니다")
    private UUID questionId;
    
    /**
     * 선택한 옵션 ID
     */
    @NotNull(message = "선택한 옵션 ID는 필수입니다")
    private UUID selectedOptionId;
    
    /**
     * 소요 시간(초)
     */
    @Min(value = 0, message = "소요 시간은 0초 이상이어야 합니다")
    private int timeSpentInSeconds;
}
