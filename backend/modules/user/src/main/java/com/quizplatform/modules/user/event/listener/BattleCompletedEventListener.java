package com.quizplatform.modules.user.event.listener;

import com.quizplatform.modules.battle.event.BattleCompletedEvent;
import com.quizplatform.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Battle 모듈에서 발생하는 BattleCompletedEvent를 처리하는 이벤트 리스너입니다.
 * 배틀 완료 시 사용자의 경험치를 업데이트하는 작업을 수행합니다.
 *
 * @author Claude
 * @since JDK 17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BattleCompletedEventListener {

    private final UserService userService;

    /**
     * BattleCompletedEvent 이벤트를 수신하여 처리합니다.
     * 승자에게 경험치를 추가하고, 패자에게는 작은 위로 경험치를 제공합니다.
     *
     * @param event 수신된 BattleCompletedEvent
     */
    @EventListener
    public void handleBattleCompletedEvent(BattleCompletedEvent event) {
        log.info("Battle completed event received: Battle ID={}, Winner={}, Loser={}, XP={}", 
                 event.getBattleId(), event.getWinnerId(), event.getLoserId(), event.getExperienceGained());
        
        try {
            // 승자에게 획득한 경험치 추가
            userService.addExperience(event.getWinnerId(), event.getExperienceGained());
            
            // 패자에게도 위로 경험치 제공 (획득 경험치의 10%)
            int consolationXp = (int)(event.getExperienceGained() * 0.1);
            userService.addExperience(event.getLoserId(), consolationXp);
            
            log.info("Successfully updated user experience after battle. Winner XP: {}, Loser XP: {}", 
                     event.getExperienceGained(), consolationXp);
        } catch (Exception e) {
            log.error("Error processing battle completed event", e);
        }
    }
}
