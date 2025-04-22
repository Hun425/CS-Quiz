package com.quizplatform.battle.application.service;

import com.quizplatform.battle.application.port.in.GetBattleUseCase;
import com.quizplatform.battle.application.port.in.GetBattleSummaryUseCase;
import com.quizplatform.battle.application.port.out.LoadBattlePort;
import com.quizplatform.battle.application.port.out.LoadBattleSummaryPort;
import com.quizplatform.battle.domain.model.Battle;
import com.quizplatform.battle.domain.model.BattleSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 배틀 조회 관련 서비스 구현
 */
@Service
@RequiredArgsConstructor
public class BattleQueryService implements GetBattleUseCase, GetBattleSummaryUseCase {

    private final LoadBattlePort loadBattlePort;
    private final LoadBattleSummaryPort loadBattleSummaryPort;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Battle getBattleById(UUID battleId) {
        return loadBattlePort.loadBattleById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("배틀을 찾을 수 없습니다: " + battleId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Battle> getBattlesByUserId(UUID userId) {
        return loadBattlePort.loadBattlesByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Battle> getRecentBattles(int limit) {
        return loadBattlePort.loadRecentBattles(limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BattleSummary getBattleSummaryById(UUID battleId) {
        return loadBattleSummaryPort.loadBattleSummaryByBattleId(battleId)
                .orElseThrow(() -> new IllegalArgumentException("배틀 요약 정보를 찾을 수 없습니다: " + battleId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BattleSummary> getBattleSummariesByUserId(UUID userId) {
        return loadBattleSummaryPort.loadBattleSummariesByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<BattleSummary> getRecentBattleSummaries(int limit) {
        return loadBattleSummaryPort.loadRecentBattleSummaries(limit);
    }
}