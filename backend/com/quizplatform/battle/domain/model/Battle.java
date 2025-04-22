package com.quizplatform.battle.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Battle 도메인 엔티티
 * 두 사용자 간의 퀴즈 대결을 표현
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Battle {
    private final UUID id;
    private final UUID challengerId;
    private final UUID opponentId;
    private final UUID quizId;
    private final int timeLimit;
    private BattleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Builder.Default
    private List<BattleParticipant> participants = new ArrayList<>();

    /**
     * 새로운 Battle 인스턴스 생성
     * 
     * @param challengerId 배틀을 도전한 사용자 ID
     * @param opponentId 대결 상대 사용자 ID
     * @param quizId 대결에 사용할 퀴즈 ID
     * @param timeLimit 제한 시간(초)
     * @return 생성된 Battle 인스턴스
     */
    public static Battle create(UUID challengerId, UUID opponentId, UUID quizId, int timeLimit) {
        return Battle.builder()
                .id(UUID.randomUUID())
                .challengerId(challengerId)
                .opponentId(opponentId)
                .quizId(quizId)
                .timeLimit(timeLimit)
                .status(BattleStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 배틀 시작
     * 
     * @return 업데이트된 Battle 인스턴스
     */
    public Battle start() {
        if (this.status != BattleStatus.CREATED) {
            throw new IllegalStateException("배틀은 CREATED 상태에서만 시작할 수 있습니다.");
        }
        this.status = BattleStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        return this;
    }

    /**
     * 배틀 완료
     * 
     * @return 업데이트된 Battle 인스턴스
     */
    public Battle complete() {
        if (this.status != BattleStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 배틀만 완료할 수 있습니다.");
        }
        this.status = BattleStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        return this;
    }

    /**
     * 배틀 취소
     * 
     * @return 업데이트된 Battle 인스턴스
     */
    public Battle cancel() {
        if (this.status == BattleStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 배틀은 취소할 수 없습니다.");
        }
        this.status = BattleStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        return this;
    }
    
    /**
     * 참가자 추가
     * 
     * @param participant 배틀 참가자
     * @return 업데이트된 Battle 인스턴스
     */
    public Battle addParticipant(BattleParticipant participant) {
        if (this.status != BattleStatus.CREATED && this.status != BattleStatus.IN_PROGRESS) {
            throw new IllegalStateException("생성되었거나 진행 중인 배틀에만 참가자를 추가할 수 있습니다.");
        }
        this.participants.add(participant);
        return this;
    }
    
    /**
     * 승자 결정
     * 
     * @return 승자 ID
     */
    public UUID determineWinner() {
        if (this.status != BattleStatus.COMPLETED) {
            throw new IllegalStateException("완료된 배틀에서만 승자를 결정할 수 있습니다.");
        }
        
        BattleParticipant challenger = null;
        BattleParticipant opponent = null;
        
        for (BattleParticipant participant : participants) {
            if (participant.getUserId().equals(challengerId)) {
                challenger = participant;
            } else if (participant.getUserId().equals(opponentId)) {
                opponent = participant;
            }
        }
        
        if (challenger == null || opponent == null) {
            throw new IllegalStateException("두 참가자가 모두 있어야 승자를 결정할 수 있습니다.");
        }
        
        if (challenger.getScore() > opponent.getScore()) {
            return challengerId;
        } else if (challenger.getScore() < opponent.getScore()) {
            return opponentId;
        } else {
            // 동점인 경우 먼저 완료한 사람이 승리
            if (challenger.getCompletionTime().isBefore(opponent.getCompletionTime())) {
                return challengerId;
            } else {
                return opponentId;
            }
        }
    }
}