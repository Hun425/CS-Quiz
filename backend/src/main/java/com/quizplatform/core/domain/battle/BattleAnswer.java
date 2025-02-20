package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.question.Question;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "battle_answers")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BattleAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private BattleParticipant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "is_correct")
    private boolean correct;

    @Column(name = "earned_points")
    private int earnedPoints;

    @Column(name = "time_bonus")
    private int timeBonus;

    @Column(name = "time_taken")
    private int timeTaken;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public BattleAnswer(BattleParticipant participant, Question question, String answer, int timeTaken) {
        this.participant = participant;
        this.question = question;
        this.answer = answer;
        this.timeTaken = timeTaken;
        this.timeBonus = 0;
        this.earnedPoints = 0;
    }

    // 정답 여부 설정
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    // 획득 점수 설정
    public void setEarnedPoints(int points) {
        this.earnedPoints = points;
    }

    // 시간 보너스 설정
    public void setTimeBonus(int bonus) {
        this.timeBonus = bonus;
    }

    // 총 획득 점수 계산
    public int getTotalPoints() {
        return earnedPoints + timeBonus;
    }
}