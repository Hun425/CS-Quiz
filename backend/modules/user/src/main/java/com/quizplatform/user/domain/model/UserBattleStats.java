package com.quizplatform.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

/**
 * 사용자 배틀 통계 엔티티
 * 
 * <p>사용자의 배틀 관련 통계 정보를 관리합니다.
 * 승/패/무승부 횟수, 연승 기록, ELO 점수 등의 정보를 포함합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "user_battle_stats", schema = "user_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBattleStats {
    
    /**
     * 배틀 통계 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 사용자
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * 승리 횟수
     */
    @Column(nullable = false)
    private int wins;
    
    /**
     * 패배 횟수
     */
    @Column(nullable = false)
    private int losses;
    
    /**
     * 무승부 횟수
     */
    @Column(nullable = false)
    private int draws;
    
    /**
     * 최대 연승 횟수
     */
    @Column(name = "max_win_streak", nullable = false)
    private int maxWinStreak;
    
    /**
     * 현재 연승 횟수
     */
    @Column(name = "current_win_streak", nullable = false)
    private int currentWinStreak;
    
    /**
     * ELO 점수
     */
    @Column(name = "elo_rating", nullable = false)
    private int eloRating;
    
    /**
     * 총 배틀 횟수
     */
    @Column(name = "total_battles", nullable = false)
    private int totalBattles;
    
    /**
     * 마지막 배틀 시간
     */
    @Column(name = "last_battle_time")
    private ZonedDateTime lastBattleTime;
    
    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    /**
     * 사용자 배틀 통계 생성자
     * 
     * @param user 사용자
     */
    public UserBattleStats(User user) {
        this.user = user;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
        this.maxWinStreak = 0;
        this.currentWinStreak = 0;
        this.eloRating = 1000; // 기본 ELO 점수
        this.totalBattles = 0;
    }
    
    /**
     * 승리 처리
     * 
     * @param eloChange ELO 점수 변화량
     */
    public void addWin(int eloChange) {
        this.wins++;
        this.totalBattles++;
        this.currentWinStreak++;
        this.eloRating += eloChange;
        this.lastBattleTime = ZonedDateTime.now();
        
        if (this.currentWinStreak > this.maxWinStreak) {
            this.maxWinStreak = this.currentWinStreak;
        }
    }
    
    /**
     * 패배 처리
     * 
     * @param eloChange ELO 점수 변화량
     */
    public void addLoss(int eloChange) {
        this.losses++;
        this.totalBattles++;
        this.currentWinStreak = 0;
        this.eloRating -= eloChange;
        this.lastBattleTime = ZonedDateTime.now();
    }
    
    /**
     * 무승부 처리
     * 
     * @param eloChange ELO 점수 변화량
     */
    public void addDraw(int eloChange) {
        this.draws++;
        this.totalBattles++;
        this.eloRating += eloChange;
        this.lastBattleTime = ZonedDateTime.now();
    }
} 