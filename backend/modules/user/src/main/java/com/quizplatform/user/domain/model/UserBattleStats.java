package com.quizplatform.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 배틀 통계 엔티티 클래스
 * 
 * <p>사용자의 배틀 참여 기록과 통계 정보를 관리합니다.
 * 승패 기록, 랭킹 정보, 연승/연패 기록 등을 포함합니다.</p>
 */
@Entity
@Table(name = "user_battle_stats", schema = "user_schema")
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
     * 통계가 연결된 사용자
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 총 배틀 참여 횟수
     */
    @Column(name = "total_battles", nullable = false)
    private int totalBattles;

    /**
     * 승리 횟수
     */
    @Column(name = "wins", nullable = false)
    private int wins;

    /**
     * 패배 횟수
     */
    @Column(name = "losses", nullable = false)
    private int losses;

    /**
     * 무승부 횟수
     */
    @Column(name = "draws", nullable = false)
    private int draws;

    /**
     * 현재 연승 횟수
     */
    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    /**
     * 최대 연승 기록
     */
    @Column(name = "max_win_streak", nullable = false)
    private int maxWinStreak;

    /**
     * 랭킹 포인트
     */
    @Column(name = "ranking_points", nullable = false)
    private int rankingPoints;

    /**
     * 최근 배틀 결과 (W: 승리, L: 패배, D: 무승부)
     */
    @Column(name = "recent_results", length = 20)
    private String recentResults;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 배틀 통계 생성자
     * 
     * @param user 연결될 사용자
     */
    @Builder
    public UserBattleStats(User user) {
        this.user = user;
        this.totalBattles = 0;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
        this.currentStreak = 0;
        this.maxWinStreak = 0;
        this.rankingPoints = 0;
        this.recentResults = "";
    }

    /**
     * 승리 기록 추가
     * 
     * @param pointsGained 획득한 랭킹 포인트
     */
    public void addWin(int pointsGained) {
        this.totalBattles++;
        this.wins++;
        this.currentStreak = currentStreak >= 0 ? currentStreak + 1 : 1;
        this.maxWinStreak = Math.max(this.maxWinStreak, this.currentStreak);
        this.rankingPoints += pointsGained;
        updateRecentResults('W');
    }

    /**
     * 패배 기록 추가
     * 
     * @param pointsLost 잃은 랭킹 포인트
     */
    public void addLoss(int pointsLost) {
        this.totalBattles++;
        this.losses++;
        this.currentStreak = currentStreak <= 0 ? currentStreak - 1 : -1;
        this.rankingPoints = Math.max(0, this.rankingPoints - pointsLost);
        updateRecentResults('L');
    }

    /**
     * 무승부 기록 추가
     */
    public void addDraw() {
        this.totalBattles++;
        this.draws++;
        // 연승/연패 스트릭은 유지
        updateRecentResults('D');
    }

    /**
     * 최근 결과 문자열 업데이트
     * 
     * @param result 결과 문자 (W/L/D)
     */
    private void updateRecentResults(char result) {
        if (this.recentResults == null) {
            this.recentResults = "";
        }
        
        // 최근 10개 결과만 유지
        if (this.recentResults.length() >= 10) {
            this.recentResults = this.recentResults.substring(1);
        }
        
        this.recentResults += result;
    }

    /**
     * 승률 계산
     * 
     * @return 승률 (0.0 ~ 1.0)
     */
    public double getWinRate() {
        if (totalBattles == 0) {
            return 0.0;
        }
        return (double) wins / totalBattles;
    }
} 