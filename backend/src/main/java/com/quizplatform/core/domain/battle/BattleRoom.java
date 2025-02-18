package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

// 실시간 대결방 도메인
@Entity
@Table(name = "battle_rooms")
public class BattleRoom {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private BattleRoomStatus status;

    @ManyToOne
    private Quiz quiz;

    private int maxParticipants;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int timeLimit; // 분 단위

    @OneToMany(mappedBy = "battleRoom")
    private Set<BattleParticipant> participants;

    // 대결방 생성 정적 팩토리 메서드
    public static BattleRoom create(Quiz quiz, int maxParticipants, int timeLimit) {
        BattleRoom room = new BattleRoom();
        room.id = UUID.randomUUID();
        room.quiz = quiz;
        room.maxParticipants = maxParticipants;
        room.timeLimit = timeLimit;
        room.status = BattleRoomStatus.WAITING;
        room.participants = new HashSet<>();
        return room;
    }

    // 참가자 추가 메서드
    public void addParticipant(User user) {
        if (participants.size() >= maxParticipants) {
            throw new BattleRoomFullException();
        }
        participants.add(new BattleParticipant(this, user));

        // 방이 다 찼다면 게임 시작
        if (participants.size() == maxParticipants) {
            startBattle();
        }
    }

    // 대결 시작 메서드
    private void startBattle() {
        this.status = BattleRoomStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.endTime = startTime.plusMinutes(timeLimit);
    }
}
