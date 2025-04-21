package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배틀 결과 응답 DTO
 * <p>
 * 배틀 결과 조회 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResultResponse {
    private String battleId;
    private String battleName;
    private Long quizId;
    private String quizTitle;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer totalPlayers;
    private List<BattlePlayerResultResponse> playerResults;
}