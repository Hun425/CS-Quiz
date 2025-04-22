package com.quizplatform.battle.application.port.in;

import com.quizplatform.battle.domain.model.Battle;

import java.util.List;
import java.util.UUID;

/**
 * 배틀 조회 유스케이스
 */
public interface GetBattleUseCase {
    /**
     * 배틀 ID로 배틀 조회
     * 
     * @param battleId 배틀 ID
     * @return 조회된 배틀 객체
     */
    Battle getBattleById(UUID battleId);
    
    /**
     * 사용자 ID로 참여한 배틀 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자가 참여한 배틀 목록
     */
    List<Battle> getBattlesByUserId(UUID userId);
    
    /**
     * 최근 배틀 목록 조회
     * 
     * @param limit 조회할 배틀 수
     * @return 최근 배틀 목록
     */
    List<Battle> getRecentBattles(int limit);
}
