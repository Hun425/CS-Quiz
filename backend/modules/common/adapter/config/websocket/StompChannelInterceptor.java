package com.quizplatform.common.adapter.config.websocket;

// TODO: BattleLeaveRequest DTO, BattleService 의 최종 위치 확인 후 import 경로 재검토 필요
import com.quizplatform.battle.dto.BattleLeaveRequest; // battle 모듈 dto 로 이동 가정
import com.quizplatform.battle.service.BattleService; // battle 모듈 service 로 이동 가정 (Port/Interface 사용 고려)
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * STOMP 채널 인터셉터 - 클라이언트 연결/해제 처리
 * 
 * <p>WebSocket 연결 시작/종료 이벤트를 감지하고 처리합니다.
 * 특히 사용자 연결 해제 시, 해당 사용자가 참여 중이던 배틀룸에서 자동으로 나가도록 처리합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Component
public class StompChannelInterceptor implements ChannelInterceptor {

    /**
     * 애플리케이션 컨텍스트
     */
    private final ApplicationContext applicationContext;
    
    /**
     * 배틀 서비스 (지연 주입)
     */
    // TODO: BattleService 를 직접 주입하는 대신 Battle 도메인의 Port 인터페이스를 주입받도록 리팩토링 고려
    private final BattleService battleService;

    /**
     * 생성자
     * 
     * <p>BattleService는 순환 참조 방지를 위해 지연 주입(Lazy)합니다.</p>
     * 
     * @param applicationContext 애플리케이션 컨텍스트
     * @param battleService 배틀 서비스
     */
    @Autowired
    public StompChannelInterceptor(ApplicationContext applicationContext, @Lazy BattleService battleService) {
        this.applicationContext = applicationContext;
        this.battleService = battleService;
        log.info("StompChannelInterceptor: BattleService 지연 주입 설정 완료");
    }

    /**
     * 메시지 전송 완료 후 처리
     * 
     * <p>클라이언트 연결 해제 시 호출되며, 사용자가 배틀룸에서 나가는 처리를 수행합니다.</p>
     * 
     * @param message 메시지
     * @param channel 메시지 채널
     * @param sent 전송 성공 여부
     * @param ex 발생한 예외 (있는 경우)
     */
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

            if (userId != null && roomId != null && battleService != null) {
                Long userIdLong = (Long) userId;
                Long roomIdLong = (Long) roomId;

                log.info("사용자 연결 해제 처리: userId={}, roomId={}", userIdLong, roomIdLong);

                try {
                    // 배틀룸 나가기 처리
                    // TODO: BattleLeaveRequest DTO 위치 확인
                    BattleLeaveRequest request = new BattleLeaveRequest(roomIdLong, userIdLong);
                    // TODO: BattleService 위치 확인 (Port 사용 고려)
                    battleService.leaveBattle(request, sessionId);
                } catch (Exception e) {
                    log.error("연결 해제 시 배틀룸 나가기 처리 중 오류 발생", e);
                }
            }
        }
    }
} 