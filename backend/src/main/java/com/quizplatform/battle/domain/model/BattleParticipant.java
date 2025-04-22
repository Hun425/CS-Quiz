package com.quizplatform.battle.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Battle 참가자를 표현하는 도메인 엔티티
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BattleParticipant {
    private final UUID id;
    private final UUID userId;
    private final UUID battleId;
    private int score;
    private int correctAnswers;
    private int totalQuestions;
    private LocalDateTime joinTime;
    private LocalDateTime completionTime;
    private ParticipantStatus status;

    /**
     * 새로운 BattleParticipant 인스턴스 생성
     * 
     * @param userId 사용자 ID
     * @param battleId 배틀 ID
     * @param totalQuestions 총 문제 수
     * @return 생성된 BattleParticipant 인스턴스
     */
    public static BattleParticipant create(UUID userId, UUID battleId, int totalQuestions) {
        return BattleParticipant.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .battleId(battleId)
                .score(0)
                .correctAnswers(0)
                .totalQuestions(totalQuestions)
                .joinTime(LocalDateTime.now())
                .status(ParticipantStatus.JOINED)
                .build();
    }

    /**
     * 정답 추가
     * 
     * @param isCorrect 정답 여부
     * @param earnedPoints 획득한 점수
     * @return 업데이트된 BattleParticipant 인스턴스
     */
    public BattleParticipant addAnswer(boolean isCorrect, int earnedPoints) {
        if (this.status != ParticipantStatus.JOINED && this.status != ParticipantStatus.IN_PROGRESS) {
            throw new IllegalStateException("참여 중인 상태에서만 답변을 추가할 수 있습니다.");
        }
        
        if (isCorrect) {
            this.correctAnswers++;
            this.score += earnedPoints;
        }
        
        this.status = ParticipantStatus.IN_PROGRESS;
        return this;
    }

    /**
     * 배틀 완료 처리
     * 
     * @return 업데이트된 BattleParticipant 인스턴스
     */
    public BattleParticipant complete() {
        if (this.status == ParticipantStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 상태입니다.");
        }
        
        this.status = ParticipantStatus.COMPLETED;
        this.completionTime = LocalDateTime.now();
        return this;
    }

    /**
     * 배틀 포기 처리
     * 
     * @return 업데이트된 BattleParticipant 인스턴스
     */
    public BattleParticipant forfeit() {
        if (this.status == ParticipantStatus.COMPLETED || this.status == ParticipantStatus.FORFEITED) {
            throw new IllegalStateException("이미 완료되거나 포기한 상태입니다.");
        }
        
        this.status = ParticipantStatus.FORFEITED;
        this.completionTime = LocalDateTime.now();
        return this;
    }
}
