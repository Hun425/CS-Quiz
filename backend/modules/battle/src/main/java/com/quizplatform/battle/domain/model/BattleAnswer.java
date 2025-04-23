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
 * <p>배틀 중 참가자가 제출한 각 문제에 대한 답변을 관리합니다.
 * 답변 내용, 정확도, 제출 시간 등의 정보를 포함합니다.</p>
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
     * 연결된 배틀 참가자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private BattleParticipant participant;

    /**
     * 문제 ID (퀴즈 모듈의 Question 참조)
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 배틀 내 문제 순서 (인덱스)
     */
    @Column(name = "question_index", nullable = false)
    private int questionIndex;

    /**
     * 사용자 제출 답변
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;

    /**
     * 정답 여부
     */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    /**
     * 획득 점수
     */
    @Column(nullable = false)
    private double score;

    /**
     * 답변 제출 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 답변 소요 시간 (초)
     */
    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    /**
     * 답변 생성자
     * 
     * @param participant 배틀 참가자
     * @param questionId 문제 ID
     * @param questionIndex 문제 인덱스
     * @param answer 제출 답변
     * @param isCorrect 정답 여부
     * @param score 획득 점수
     * @param timeTakenSeconds 소요 시간
     */
    @Builder
    public BattleAnswer(BattleParticipant participant, Long questionId, int questionIndex, 
                       String answer, boolean isCorrect, double score, Integer timeTakenSeconds) {
        this.participant = participant;
        this.questionId = questionId;
        this.questionIndex = questionIndex;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.score = score;
        this.timeTakenSeconds = timeTakenSeconds;
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
        this.timeTakenSeconds = timeTakenSeconds;
    }
} 