package com.quizplatform.battle.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 배틀 답변 엔티티 클래스
 * 
 * <p>배틀 참가자가 문제에 제출한 답변 정보를 저장합니다.
 * 문제 인덱스, 답변 내용, 정답 여부, 소요 시간, 획득 점수 등을 포함합니다.</p>
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "battle_answers", schema = "battle_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BattleAnswer {

    /**
     * 답변 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 참가자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private BattleParticipant participant;

    /**
     * 문제 인덱스
     */
    @Column(name = "question_index", nullable = false)
    private int questionIndex;

    /**
     * 제출한 답변
     */
    @Column(name = "answer", length = 500, nullable = false)
    private String answer;

    /**
     * 정답 여부
     */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    /**
     * 답변 소요 시간(ms)
     */
    @Column(name = "answer_time_ms", nullable = false)
    private long answerTime;

    /**
     * 획득 점수
     */
    @Column(name = "score", nullable = false)
    private double score;

    /**
     * 제출 시간
     */
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 배틀 답변 생성자
     * 
     * @param participant 답변 제출 참가자
     * @param questionIndex 문제 인덱스
     * @param answer 제출한 답변
     * @param isCorrect 정답 여부
     * @param answerTime 답변 소요 시간(ms)
     * @param score 획득 점수
     * @param submittedAt 제출 시간
     */
    @Builder
    public BattleAnswer(BattleParticipant participant, int questionIndex, String answer,
                       boolean isCorrect, long answerTime, double score, LocalDateTime submittedAt) {
        this.participant = participant;
        this.questionIndex = questionIndex;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.answerTime = answerTime;
        this.score = score;
        this.submittedAt = submittedAt != null ? submittedAt : LocalDateTime.now();
    }

    /**
     * 점수 업데이트
     * 
     * @param score 새 점수
     * @param isCorrect 정답 여부
     */
    public void updateScore(double score, boolean isCorrect) {
        this.score = score;
        this.isCorrect = isCorrect;
    }

    /**
     * 부분 점수 계산 여부 확인
     * 
     * @return 부분 점수인 경우 true
     */
    public boolean isPartialScore() {
        return isCorrect && score < 1.0;
    }

    /**
     * 답변 내용과 시간 정보 업데이트
     * 
     * @param answer 새 답변 내용
     * @param timeTakenSeconds 새 소요 시간
     */
    public void updateAnswer(String answer, Integer timeTakenSeconds) {
        this.answer = answer;
        this.answerTime = timeTakenSeconds != null ? timeTakenSeconds.longValue() : 0;
    }
} 