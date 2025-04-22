package com.quizplatform.battle.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 배틀 시작 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartBattleRequest {
    
    /**
     * 사용자 ID
     */
    @NotNull(message = "사용자 ID는 필수입니다")
    private UUID userId;
}
