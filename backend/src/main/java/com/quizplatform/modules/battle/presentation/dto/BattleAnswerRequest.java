package com.quizplatform.modules.battle.presentation.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 배틀 모드에서 사용자 답변 제출 요청 DTO
 * 
 * <p>사용자가 배틀 모드 게임에서 문제에 대한 답변을 제출할 때 사용됩니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
@Builder
public class BattleAnswerRequest {
    /**
     * 배틀룸 ID
     */
    private Long roomId;
    
    /**
     * 문제 ID
     */
    private Long questionId;
    
    /**
     * 사용자가 선택한 답변
     */
    private String answer;
    
    /**
     * 답변 제출에 소요된 시간(초)
     */
    private int timeSpentSeconds;
}
