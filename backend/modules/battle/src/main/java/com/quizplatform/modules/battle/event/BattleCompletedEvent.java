package com.quizplatform.modules.battle.event;

import com.quizplatform.core.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * 퀴즈 대결이 완료되었을 때 발생하는 도메인 이벤트입니다.
 * 대결 완료 정보를 포함하며, 다른 모듈에서 이 이벤트를 구독하여 처리할 수 있습니다.
 *
 * @author Claude
 * @since JDK 17
 */
@Getter
public class BattleCompletedEvent extends BaseDomainEvent {
    
    /** 대결 ID */
    private final UUID battleId;
    
    /** 승자 사용자 ID */
    private final UUID winnerId;
    
    /** 패자 사용자 ID */
    private final UUID loserId;
    
    /** 획득한 경험치 */
    private final int experienceGained;
    
    /**
     * BattleCompletedEvent 생성자
     *
     * @param source 이벤트 소스 객체
     * @param battleId 완료된 대결의 ID
     * @param winnerId 대결 승자의 사용자 ID
     * @param loserId 대결 패자의 사용자 ID
     * @param experienceGained 획득한 경험치
     */
    public BattleCompletedEvent(Object source, UUID battleId, UUID winnerId, UUID loserId, int experienceGained) {
        super(source);
        this.battleId = battleId;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.experienceGained = experienceGained;
    }
}
