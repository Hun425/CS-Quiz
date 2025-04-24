package com.quizplatform.battle.infrastructure.kafka;

import com.quizplatform.battle.domain.event.BattleCompletedEvent;
import com.quizplatform.battle.domain.model.BattleRoom;
import com.quizplatform.common.event.EventPublisher;
import com.quizplatform.common.event.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 배틀 관련 이벤트를 Kafka 토픽으로 발행하는 프로듀서 클래스
 * 
 * <p>다른 서비스 모듈과의 비동기 통신을 위해 이벤트를 발행합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BattleEventProducer {

    private final EventPublisher eventPublisher;
    
    /**
     * 배틀 완료 이벤트를 Kafka 토픽으로 발행합니다.
     * 
     * @param battleRoom 완료된 배틀 방
     */
    public void publishBattleCompletedEvent(BattleRoom battleRoom) {
        try {
            // 도메인 이벤트 생성
            BattleCompletedEvent battleCompletedEvent = new BattleCompletedEvent(battleRoom);
            
            // 공통 모듈의 이벤트 객체로 변환
            com.quizplatform.common.event.battle.BattleCompletedEvent commonEvent = 
                new com.quizplatform.common.event.battle.BattleCompletedEvent(
                    battleRoom.getId().toString(),
                    battleRoom.getWinner() != null ? battleRoom.getWinner().getUserId().toString() : null
                );
            
            // 이벤트 발행
            eventPublisher.publish(commonEvent, Topics.BATTLE_COMPLETED);
            
            log.info("배틀 완료 이벤트 발행 완료 - 배틀 ID: {}, 이벤트 ID: {}", 
                    battleRoom.getId(), commonEvent.getEventId());
        } catch (Exception e) {
            log.error("배틀 완료 이벤트 발행 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 배틀 시작 이벤트를 Kafka 토픽으로 발행합니다.
     * 
     * @param battleRoom 시작된 배틀 방
     */
    public void publishBattleStartedEvent(BattleRoom battleRoom) {
        try {
            // 공통 모듈의 이벤트 객체 생성
            com.quizplatform.common.event.battle.BattleStartedEvent commonEvent = 
                new com.quizplatform.common.event.battle.BattleStartedEvent(
                    battleRoom.getId().toString()
                );
            
            // 이벤트 발행
            eventPublisher.publish(commonEvent, Topics.BATTLE_STARTED);
            
            log.info("배틀 시작 이벤트 발행 완료 - 배틀 ID: {}, 이벤트 ID: {}", 
                    battleRoom.getId(), commonEvent.getEventId());
        } catch (Exception e) {
            log.error("배틀 시작 이벤트 발행 실패: {}", e.getMessage(), e);
        }
    }
} 