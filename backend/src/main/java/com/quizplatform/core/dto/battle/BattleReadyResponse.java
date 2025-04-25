package com.quizplatform.core.dto.battle;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BattleReadyResponse {
    private Long roomId;
    private String type = "READY";
    private List<ParticipantInfo> participants;

    /**
     * 준비 완료된 참가자 수를 반환합니다.
     *
     * @return 준비 완료된 참가자 수
     */
    public int getReadyCount() {
        if (participants == null) {
            return 0;
        }
        return (int) participants.stream()
                .filter(ParticipantInfo::isReady)
                .count();
    }

    /**
     * 전체 참가자 수를 반환합니다.
     *
     * @return 전체 참가자 수
     */
    public int getTotalParticipants() {
        return participants != null ? participants.size() : 0;
    }

    @Getter
    @Builder
    public static class ParticipantInfo {
        private Long userId;
        private String username;
        private String profileImage;
        private int level;
        private boolean isReady;
    }
}