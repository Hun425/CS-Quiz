package com.quizplatform.core.domain.user;

import com.quizplatform.core.domain.battle.BattleParticipant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_battle_stats")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBattleStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int totalBattles;
    private int wins;
    private int totalScore;
    private int highestScore;
    private int totalCorrectAnswers;
    private int totalQuestions;
    private int highestStreak;
    private int currentStreak;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public UserBattleStats(User user) {
        this.user = user;
    }

    public void updateStats(BattleParticipant participant) {
        this.totalBattles++;
        this.totalScore += participant.getCurrentScore();
        this.highestScore = Math.max(this.highestScore, participant.getCurrentScore());
        this.totalCorrectAnswers += participant.getCorrectAnswersCount();
        this.totalQuestions += participant.getBattleRoom().getQuestions().size();
        this.highestStreak = Math.max(this.highestStreak, participant.getCurrentStreak());

        // 승리한 경우
        if (participant.equals(participant.getBattleRoom().getWinner())) {
            this.wins++;
            this.currentStreak++;
        } else {
            this.currentStreak = 0;
        }
    }

    public double getWinRate() {
        return totalBattles == 0 ? 0 : (double) wins / totalBattles * 100;
    }

    public double getAverageScore() {
        return totalBattles == 0 ? 0 : (double) totalScore / totalBattles;
    }

    public double getCorrectRate() {
        return totalQuestions == 0 ? 0 : (double) totalCorrectAnswers / totalQuestions * 100;
    }
}