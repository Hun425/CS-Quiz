package com.quizplatform.battle.adapter.in.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 배틀 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBattleRequest {
    
    /**
     * 도전자 ID
     */
    @NotNull(message = "챌린저 ID는 필수입니다")
    private UUID challengerId;
    
    /**
     * 상대방 ID
     */
    @NotNull(message = "상대방 ID는 필수입니다")
    private UUID opponentId;
    
    /**
     * 퀴즈 ID
     */
    @NotNull(message = "퀴즈 ID는 필수입니다")
    private UUID quizId;
    
    /**
     * 제한 시간(초)
     */
    @Min(value = 30, message = "제한 시간은 최소 30초 이상이어야 합니다")
    private int timeLimit;
}
