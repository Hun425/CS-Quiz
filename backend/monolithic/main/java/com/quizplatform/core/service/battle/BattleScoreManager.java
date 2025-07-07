package com.quizplatform.core.service.battle;

import com.quizplatform.core.dto.progress.ParticipantProgress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 배틀 진행 중 참가자들의 점수를 메모리에서 실시간으로 관리하는 컴포넌트
 * 
 * 주요 기능:
 * - 배틀 중 실시간 점수 업데이트 (메모리 기반)
 * - 배틀 종료 시 DB에 일괄 저장
 * - 동시성 문제 해결 (ConcurrentHashMap + synchronized 메서드)
 * 
 * @author 채기훈
 */
@Component
@Slf4j
public class BattleScoreManager {
    
    /**
     * 진행 중인 배틀의 참가자별 점수 정보
     * Key: roomId, Value: 참가자별 진행 상황 맵
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, ParticipantProgress>> activeBattleScores = new ConcurrentHashMap<>();
    
    /**
     * 배틀 시작 시 초기 점수 상태를 설정합니다.
     * 
     * @param roomId 배틀 방 ID
     * @param participantIds 참가자 ID 목록
     */
    public synchronized void initializeBattle(Long roomId, java.util.List<Long> participantIds) {
        log.info("Initializing battle scores for room: {}, participants: {}", roomId, participantIds);
        
        ConcurrentHashMap<Long, ParticipantProgress> participantScores = new ConcurrentHashMap<>();
        
        for (Long participantId : participantIds) {
            ParticipantProgress progress = ParticipantProgress.builder()
                    .participantId(participantId)
                    .currentScore(0)
                    .correctAnswers(0)
                    .totalAnswers(0)
                    .currentStreak(0)
                    .lastAnswerTime(null)
                    .build();
            participantScores.put(participantId, progress);
        }
        
        activeBattleScores.put(roomId, participantScores);
        log.debug("Battle scores initialized for room: {}", roomId);
    }
    
    /**
     * 참가자의 점수를 실시간으로 업데이트합니다.
     * 
     * @param roomId 배틀 방 ID
     * @param participantId 참가자 ID
     * @param scoreChange 점수 변화량
     * @param isCorrect 정답 여부
     * @param timeSpent 답변 소요 시간 (초)
     */
    public synchronized void updateParticipantScore(Long roomId, Long participantId, int scoreChange, boolean isCorrect, int timeSpent) {
        ConcurrentHashMap<Long, ParticipantProgress> battleScores = activeBattleScores.get(roomId);
        
        if (battleScores == null) {
            log.warn("Battle scores not found for room: {}", roomId);
            return;
        }
        
        ParticipantProgress progress = battleScores.get(participantId);
        if (progress == null) {
            log.warn("Participant progress not found for room: {}, participant: {}", roomId, participantId);
            return;
        }
        
        // 점수 업데이트
        progress.setCurrentScore(progress.getCurrentScore() + scoreChange);
        progress.setTotalAnswers(progress.getTotalAnswers() + 1);
        progress.setLastAnswerTime(java.time.LocalDateTime.now());
        
        if (isCorrect) {
            progress.setCorrectAnswers(progress.getCorrectAnswers() + 1);
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
        } else {
            progress.setCurrentStreak(0); // 연속 정답 초기화
        }
        
        log.debug("Updated score for room: {}, participant: {}, new score: {}, correct: {}", 
                roomId, participantId, progress.getCurrentScore(), isCorrect);
    }
    
    /**
     * 현재 배틀의 실시간 점수 현황을 조회합니다.
     * 
     * @param roomId 배틀 방 ID
     * @return 참가자별 진행 상황 맵
     */
    public Map<Long, ParticipantProgress> getBattleProgress(Long roomId) {
        ConcurrentHashMap<Long, ParticipantProgress> battleScores = activeBattleScores.get(roomId);
        
        if (battleScores == null) {
            log.warn("Battle scores not found for room: {}", roomId);
            return new ConcurrentHashMap<>();
        }
        
        // 방어적 복사로 원본 데이터 보호
        return new ConcurrentHashMap<>(battleScores);
    }
    
    /**
     * 특정 참가자의 현재 점수를 조회합니다.
     * 
     * @param roomId 배틀 방 ID
     * @param participantId 참가자 ID
     * @return 현재 점수, 참가자가 없으면 0
     */
    public int getCurrentScore(Long roomId, Long participantId) {
        Map<Long, ParticipantProgress> battleScores = activeBattleScores.get(roomId);
        
        if (battleScores == null) {
            return 0;
        }
        
        ParticipantProgress progress = battleScores.get(participantId);
        return progress != null ? progress.getCurrentScore() : 0;
    }
    
    /**
     * 배틀 종료 시 메모리에서 최종 점수 데이터를 가져오고 캐시를 정리합니다.
     * 
     * @param roomId 배틀 방 ID
     * @return 최종 참가자별 진행 상황, 배틀이 없으면 빈 맵
     */
    public synchronized Map<Long, ParticipantProgress> finalizeBattleAndGetResults(Long roomId) {
        log.info("Finalizing battle scores for room: {}", roomId);
        
        ConcurrentHashMap<Long, ParticipantProgress> finalScores = activeBattleScores.remove(roomId);
        
        if (finalScores == null) {
            log.warn("No battle scores found to finalize for room: {}", roomId);
            return new ConcurrentHashMap<>();
        }
        
        log.info("Battle scores finalized for room: {}, participant count: {}", roomId, finalScores.size());
        return finalScores;
    }
    
    /**
     * 배틀 중단 시 메모리 캐시를 정리합니다.
     * 
     * @param roomId 배틀 방 ID
     */
    public synchronized void cleanupBattle(Long roomId) {
        log.info("Cleaning up battle scores for room: {}", roomId);
        activeBattleScores.remove(roomId);
    }
    
    /**
     * 현재 진행 중인 배틀의 수를 반환합니다. (모니터링 용도)
     * 
     * @return 진행 중인 배틀 수
     */
    public int getActiveBattleCount() {
        return activeBattleScores.size();
    }
    
    /**
     * 특정 배틀이 진행 중인지 확인합니다.
     * 
     * @param roomId 배틀 방 ID
     * @return 진행 중이면 true
     */
    public boolean isBattleActive(Long roomId) {
        return activeBattleScores.containsKey(roomId);
    }
}