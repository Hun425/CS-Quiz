package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 배틀 생성 요청 DTO
 * <p>
 * 배틀 생성 시 사용되는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleRequest {
    private String name;
    private Integer maxPlayers;
    private Long quizId;
    private Integer timeLimit;
    private Boolean isPrivate;
    private String password;
}