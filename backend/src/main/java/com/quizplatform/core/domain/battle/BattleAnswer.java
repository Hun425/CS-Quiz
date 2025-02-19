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

// 배틀 답변 엔티티
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
    private boolean isCorrect;

    @Column(name = "answer_time")
    private LocalDateTime answerTime;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public BattleAnswer(BattleParticipant participant, Question question,
                        String answer, boolean isCorrect, LocalDateTime answerTime) {
        this.participant = participant;
        this.question = question;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.answerTime = answerTime;
    }
}
