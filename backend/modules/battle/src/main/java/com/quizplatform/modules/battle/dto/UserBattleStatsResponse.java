package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 배틀 통계 응답 DTO
 * <p>
 * 사용자의 배틀 관련 통계 정보를 담는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBattleStatsResponse {
    private Long userId;
    private String username;
    private Integer totalBattles;
    private Integer wins;
    private Integer top3;
    private Integer averageRank;
    private Integer totalScore;
    private Integer averageScore;
    private Integer winRate;
    private Integer correctAnswerRate;
    private Integer averageResponseTime;
}