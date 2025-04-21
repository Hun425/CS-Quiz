package com.quizplatform.modules.battle.service;

import com.quizplatform.modules.battle.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 배틀 서비스 인터페이스
 * <p>
 * 배틀 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * </p>
 */
public interface BattleService {

    /**
     * 배틀 ID로 배틀 정보를 조회합니다.
     *
     * @param battleId 배틀 ID
     * @return 배틀 상세 정보
     */
    BattleDetailResponse getBattleById(String battleId);

    /**
     * 현재 활성화된 배틀 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 활성화된 배틀 목록
     */
    Page<BattleResponse> getActiveBattles(Pageable pageable);

    /**
     * 새로운 배틀을 생성합니다.
     *
     * @param battleRequest 배틀 생성 정보
     * @return 생성된 배틀 정보
     */
    BattleDetailResponse createBattle(BattleRequest battleRequest);

    /**
     * 배틀에 사용자를 참가시킵니다.
     *
     * @param battleId 배틀 ID
     * @param userId 사용자 ID
     */
    void joinBattle(String battleId, Long userId);

    /**
     * 배틀에서 사용자를 퇴장시킵니다.
     *
     * @param battleId 배틀 ID
     * @param userId 사용자 ID
     */
    void leaveBattle(String battleId, Long userId);

    /**
     * 배틀을 시작합니다.
     *
     * @param battleId 배틀 ID
     */
    void startBattle(String battleId);

    /**
     * 배틀을 종료합니다.
     *
     * @param battleId 배틀 ID
     */
    void endBattle(String battleId);

    /**
     * 배틀 결과를 조회합니다.
     *
     * @param battleId 배틀 ID
     * @return 배틀 결과
     */
    BattleResultResponse getBattleResults(String battleId);

    /**
     * 사용자의 배틀 참여 기록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 사용자의 배틀 참여 기록
     */
    Page<BattleResponse> getUserBattleHistory(Long userId, Pageable pageable);

    /**
     * 사용자의 배틀 통계를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 배틀 통계
     */
    UserBattleStatsResponse getUserBattleStats(Long userId);
}