package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.quiz.Question;
import com.quizplatform.core.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

// 실시간 대결 참가자 도메인
@Entity
@Table(name = "battle_participants")
public class BattleParticipant {
    @Id
    private UUID id;

    @ManyToOne
    private BattleRoom battleRoom;

    @ManyToOne
    private User user;

    private int currentScore;
    private int questionsAnswered;
    private LocalDateTime lastAnswerTime;

    // 문제 답변 메서드
    public void answerQuestion(Question question, String answer) {
        questionsAnswered++;
        if (question.isCorrectAnswer(answer)) {
            // 점수 계산 (남은 시간에 따른 보너스 점수 포함)
            int timeBonus = calculateTimeBonus();
            currentScore += question.getPoints() + timeBonus;
        }
        lastAnswerTime = LocalDateTime.now();
    }

    private int calculateTimeBonus() {
        // 남은 시간에 따른 보너스 점수 계산 로직
        long remainingSeconds = ChronoUnit.SECONDS.between(
                LocalDateTime.now(),
                battleRoom.getEndTime()
        );
        return (int) (remainingSeconds / 10); // 예: 남은 시간 10초당 1점 보너스
    }
}
