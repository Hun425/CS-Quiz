package com.quizplatform.modules.battle.dto;

import com.quizplatform.modules.battle.domain.BattleParticipant;
import com.quizplatform.modules.battle.domain.BattleRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 대결방 응답 DTO
 */
@Getter
@Builder
public class BattleRoomResponse {
    private Long id;
    private String roomCode;
    private String quizTitle;
    private Long quizId;
    private String status;
    private int maxParticipants;
    private int currentParticipants;
    private List<ParticipantDto> participants;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer timeLimit;
    private int questionCount;
    private Long creatorId;

    /**
     * BattleRoom 엔티티를 BattleRoomResponse DTO로 변환
     */
    public static BattleRoomResponse from(BattleRoom battleRoom) {
        List<ParticipantDto> participantDtos = battleRoom.getParticipants().stream()
                .map(ParticipantDto::from)
                .collect(Collectors.toList());

        return BattleRoomResponse.builder()
                .id(battleRoom.getId())
                .roomCode(battleRoom.getRoomCode())
                .quizTitle(battleRoom.getQuiz().getTitle())
                .quizId(battleRoom.getQuiz().getId())
                .status(battleRoom.getStatus().name())
                .maxParticipants(battleRoom.getMaxParticipants())
                .currentParticipants(battleRoom.getParticipants().size())
                .participants(participantDtos)
                .createdAt(battleRoom.getCreatedAt())
                .startTime(battleRoom.getStartTime())
                .endTime(battleRoom.getEndTime())
                .timeLimit(battleRoom.getQuiz().getTimeLimit())
                .questionCount(battleRoom.getQuiz().getQuestions().size())
                .creatorId(battleRoom.getCreatorId())
                .build();
    }

    /**
     * 참가자 정보 DTO
     */
    @Getter
    @Builder
    public static class ParticipantDto {
        private Long id;
        private Long userId;
        private String username;
        private String profileImage;
        private boolean ready;
        private int level;
        private int currentScore;

        /**
         * BattleParticipant 엔티티를 ParticipantDto로 변환
         */
        public static ParticipantDto from(BattleParticipant participant) {
            return ParticipantDto.builder()
                    .id(participant.getId())
                    .userId(participant.getUser().getId())
                    .username(participant.getUser().getUsername())
                    .profileImage(participant.getUser().getProfileImage())
                    .ready(participant.isReady())
                    .level(participant.getUser().getLevel())
                    .currentScore(participant.getCurrentScore())
                    .build();
        }
    }
}
