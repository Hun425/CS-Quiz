package com.quizplatform.common.event.quiz;

import com.quizplatform.common.event.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 사용자 업적 이벤트
 * 
 * <p>사용자가 특정 업적을 달성했을 때 발생하는 이벤트입니다.
 * 퀴즈 모듈에서 발행하고, 사용자 모듈에서 수신하여 보너스 경험치와 포인트를 부여합니다.</p>
 */
@Getter
@ToString
@AllArgsConstructor
public class UserAchievementEvent implements DomainEvent {
    private final String eventId;
    private final long timestamp;
    private final String userId;
    private final String achievementType;
    private final String achievementDescription;
    private final int bonusExperience;
    private final int bonusPoints;
    
    /**
     * 사용자 업적 이벤트 생성자
     * 
     * @param userId 사용자 ID
     * @param achievementType 업적 타입
     * @param achievementDescription 업적 설명
     * @param bonusExperience 보너스 경험치
     * @param bonusPoints 보너스 포인트
     */
    public UserAchievementEvent(String userId, String achievementType, String achievementDescription,
                                int bonusExperience, int bonusPoints) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.achievementType = achievementType;
        this.achievementDescription = achievementDescription;
        this.bonusExperience = bonusExperience;
        this.bonusPoints = bonusPoints;
    }
} 