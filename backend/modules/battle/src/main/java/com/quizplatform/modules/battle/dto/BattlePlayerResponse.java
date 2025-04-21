package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배틀 플레이어 응답 DTO
 * <p>
 * 배틀에 참여한 플레이어 정보를 포함하는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattlePlayerResponse {
    private Long userId;
    private String username;
    private String profileImage;
    private Integer score;
    private Integer rank;
    private Boolean ready;
    private Boolean isHost;
    private Boolean isActive;
}