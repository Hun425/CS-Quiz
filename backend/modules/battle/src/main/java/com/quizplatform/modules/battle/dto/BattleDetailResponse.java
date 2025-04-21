package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배틀 상세 응답 DTO
 * <p>
 * 배틀 상세 조회 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleDetailResponse {
    private String id;
    private String name;
    private String status;
    private Integer playerCount;
    private Integer maxPlayers;
    private String quizTitle;
    private Long quizId;
    private String hostUsername;
    private Long hostId;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer timeLimit;
    private List<BattlePlayerResponse> players;
    private List<BattleQuestionResponse> questions;
}