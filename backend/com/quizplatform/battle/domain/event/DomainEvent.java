package com.quizplatform.battle.domain.event;

/**
 * 도메인 이벤트를 나타내는 마커 인터페이스
 */
public interface DomainEvent {
    /**
     * 이벤트가 발생한 시간을 반환
     * @return 이벤트 발생 시간 (밀리초 단위 타임스탬프)
     */
    long getTimestamp();
}
