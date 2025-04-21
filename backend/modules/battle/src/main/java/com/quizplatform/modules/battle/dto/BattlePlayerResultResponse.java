package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배틀 플레이어 결과 응답 DTO
 * <p>
 * 배틀 결과의 플레이어별 성적 정보를 담는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattlePlayerResultResponse {
    private Long userId;
    private String username;
    private String profileImage;
    private Integer score;
    private Integer rank;
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer averageResponseTime;
    private Integer experienceGained;
}