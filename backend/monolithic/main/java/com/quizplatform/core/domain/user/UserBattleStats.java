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

/**
 * 사용자 배틀 통계 엔티티 클래스
 * 
 * <p>사용자의 퀴즈 배틀 참여 기록과 성과를 추적하는 통계 정보를 관리합니다.
 * 승률, 점수, 연승 기록 등 다양한 배틀 관련 지표를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "user_battle_stats")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBattleStats {
    /**
     * 통계 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 통계와 연결된 사용자
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 총 배틀 참여 횟수
     */
    private int totalBattles;
    
    /**
     * 승리 횟수
     */
    private int wins;
    
    /**
     * 획득한 총 점수
     */
    private int totalScore;
    
    /**
     * 최고 점수
     */
    private int highestScore;
    
    /**
     * 정답 맞힌 총 문제 수
     */
    private int totalCorrectAnswers;
    
    /**
     * 풀이한 총 문제 수
     */
    private int totalQuestions;
    
    /**
     * 최고 연승 기록
     */
    private int highestStreak;
    
    /**
     * 현재 연승 기록
     */
    private int currentStreak;

    /**
     * 통계 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 통계 최종 업데이트 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 사용자 배틀 통계 생성자
     * 
     * @param user 통계를 관리할 사용자
     */
    public UserBattleStats(User user) {
        this.user = user;
    }

    /**
     * 배틀 참여 결과를 반영하여 통계 업데이트
     * 
     * @param participant 배틀 참가자 정보
     */
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

    /**
     * 승률 계산
     * 
     * @return 승률 (0-100%)
     */
    public double getWinRate() {
        return totalBattles == 0 ? 0 : (double) wins / totalBattles * 100;
    }

    /**
     * 평균 점수 계산
     * 
     * @return 평균 점수
     */
    public double getAverageScore() {
        return totalBattles == 0 ? 0 : (double) totalScore / totalBattles;
    }

    /**
     * 정답률 계산
     * 
     * @return 정답률 (0-100%)
     */
    public double getCorrectRate() {
        return totalQuestions == 0 ? 0 : (double) totalCorrectAnswers / totalQuestions * 100;
    }
}