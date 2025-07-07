package com.quizplatform.core.dto.battle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 대결방 입장 요청 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleJoinRequest {
    private Long userId;
    private Long roomId;
    private boolean isReady;
    private Long creatorUserId; // 방 생성자 ID 필드 추가
}