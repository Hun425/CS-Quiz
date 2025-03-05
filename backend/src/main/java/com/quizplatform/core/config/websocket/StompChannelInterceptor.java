package com.quizplatform.core.config.websocket;


import com.quizplatform.core.dto.battle.BattleLeaveRequest;
import com.quizplatform.core.service.battle.BattleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * STOMP 채널 인터셉터 - 클라이언트 연결/해제 처리
 */
@Slf4j
@Component
public class StompChannelInterceptor implements ChannelInterceptor {


    private BattleService battleService;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        // ApplicationContext를 통해 BattleService를 지연 주입
        // (순환 참조 방지)
        this.battleService = applicationContext.getBean(BattleService.class);
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);

        // 클라이언트 연결 해제 처리
        if (SimpMessageType.DISCONNECT.equals(accessor.getMessageType())) {
            String sessionId = accessor.getSessionId();
            log.info("WebSocket 연결 해제 감지: sessionId={}", sessionId);

            // 세션에 저장된 사용자 정보 및 방 ID 확인
            Object userId = accessor.getSessionAttributes() != null ?
                    accessor.getSessionAttributes().get("userId") : null;
            Object roomId = accessor.getSessionAttributes() != null ?
                    accessor.getSessionAttributes().get("roomId") : null;

            if (userId != null && roomId != null) {
                Long userIdLong = (Long) userId;
                Long roomIdLong = (Long) roomId;

                log.info("사용자 연결 해제 처리: userId={}, roomId={}", userIdLong, roomIdLong);

                try {
                    // 배틀룸 나가기 처리
                    BattleLeaveRequest request = new BattleLeaveRequest(roomIdLong, userIdLong);
                    battleService.leaveBattle(request, sessionId);
                } catch (Exception e) {
                    log.error("연결 해제 시 배틀룸 나가기 처리 중 오류 발생", e);
                }
            }
        }
    }
}