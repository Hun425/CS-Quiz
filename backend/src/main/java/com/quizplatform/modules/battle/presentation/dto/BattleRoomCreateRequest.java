package com.quizplatform.modules.battle.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleRoomCreateRequest {
    @NotNull(message = "퀴즈 ID는 필수입니다.")
    private Long quizId;

    @Min(value = 2, message = "최대 참가 인원은 최소 2명 이상이어야 합니다.")
    private Integer maxParticipants;
}