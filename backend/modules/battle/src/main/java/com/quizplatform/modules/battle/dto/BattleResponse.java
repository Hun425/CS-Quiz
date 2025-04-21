package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배틀 응답 DTO
 * <p>
 * 배틀 목록 조회 시 사용되는 간략한 배틀 정보 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResponse {
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
}