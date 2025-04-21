package com.quizplatform.modules.battle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 배틀 질문 응답 DTO
 * <p>
 * 배틀에 포함된 질문 정보를 담는 DTO
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleQuestionResponse {
    private Long id;
    private String content;
    private String type;
    private Integer timeLimit;
    private Integer points;
    private List<BattleAnswerResponse> answers;
}