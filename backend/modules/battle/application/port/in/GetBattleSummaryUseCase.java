package com.quizplatform.battle.application.port.in;

import com.quizplatform.battle.domain.model.BattleSummary;

import java.util.List;
import java.util.UUID;

/**
 * 배틀 요약 정보 조회 유스케이스
 */
public interface GetBattleSummaryUseCase {
    /**
     * 배틀 ID로 배틀 요약 정보 조회
     * 
     * @param battleId 배틀 ID
     * @return 조회된 배틀 요약 정보
     */
    BattleSummary getBattleSummaryById(UUID battleId);
    
    /**
     * 사용자 ID로 참여한 배틀 요약 정보 목록 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자가 참여한 배틀 요약 정보 목록
     */
    List<BattleSummary> getBattleSummariesByUserId(UUID userId);
    
    /**
     * 최근 배틀 요약 정보 목록 조회
     * 
     * @param limit 조회할 배틀 요약 정보 수
     * @return 최근 배틀 요약 정보 목록
     */
    List<BattleSummary> getRecentBattleSummaries(int limit);
}
