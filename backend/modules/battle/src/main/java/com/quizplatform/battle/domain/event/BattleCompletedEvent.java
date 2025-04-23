package com.quizplatform.battle.domain.event;

import com.quizplatform.battle.domain.model.BattleParticipant;
import com.quizplatform.battle.domain.model.BattleRoom;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 배틀 완료 이벤트
 * 
 * <p>배틀이 완료되었을 때 발생하는 이벤트입니다.
 * 다른 모듈에서 배틀 결과를 처리할 때 사용됩니다.</p>
 */
@Getter
public class BattleCompletedEvent implements BattleEvent {
    private final String eventId;
    private final long timestamp;
    private final Long battleRoomId;
    private final Long quizId;
    private final Long winnerId;
    private final String winnerUsername;
    private final List<ParticipantResult> results;
    
    /**
     * 배틀 완료 이벤트 생성자
     * 
     * @param battleRoom 완료된 배틀 방
     */
    public BattleCompletedEvent(BattleRoom battleRoom) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.battleRoomId = battleRoom.getId();
        this.quizId = battleRoom.getQuizId();
        this.winnerId = battleRoom.getWinner() != null ? battleRoom.getWinner().getUserId() : null;
        this.winnerUsername = battleRoom.getWinner() != null ? battleRoom.getWinner().getUsername() : null;
        
        this.results = battleRoom.getParticipants().stream()
                .map(participant -> new ParticipantResult(
                        participant.getUserId(),
                        participant.getUsername(),
                        participant.getScore(),
                        participant.getRank(),
                        participant.getCorrectAnswers(),
                        participant.getWrongAnswers(),
                        participant.getEarnedPoints(),
                        participant.getEarnedExperience(),
                        participant.hasForfeited()
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "BATTLE_COMPLETED";
    }
    
    /**
     * 참가자 결과 내부 클래스
     */
    @Getter
    public static class ParticipantResult {
        private final Long userId;
        private final String username;
        private final double score;
        private final Integer rank;
        private final int correctAnswers;
        private final int wrongAnswers;
        private final Integer earnedPoints;
        private final Integer earnedExperience;
        private final boolean forfeited;
        
        public ParticipantResult(Long userId, String username, double score, Integer rank,
                                int correctAnswers, int wrongAnswers, Integer earnedPoints,
                                Integer earnedExperience, boolean forfeited) {
            this.userId = userId;
            this.username = username;
            this.score = score;
            this.rank = rank;
            this.correctAnswers = correctAnswers;
            this.wrongAnswers = wrongAnswers;
            this.earnedPoints = earnedPoints;
            this.earnedExperience = earnedExperience;
            this.forfeited = forfeited;
        }
    }
} 