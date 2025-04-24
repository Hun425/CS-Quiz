package com.quizplatform.battle.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 배틀(대결) 도메인 모델
 */
@Entity
@Table(name = "battles", schema = "battle_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Battle {
    /**
     * 배틀 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 호스트 사용자 ID
     */
    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;
    
    /**
     * 게스트 사용자 ID
     */
    @Column(name = "guest_user_id", nullable = false)
    private Long guestUserId;
    
    /**
     * 배틀에 사용되는 퀴즈 ID 목록
     */
    @ElementCollection
    @CollectionTable(
        name = "battle_quizzes",
        schema = "battle_schema",
        joinColumns = @JoinColumn(name = "battle_id")
    )
    @Column(name = "quiz_id")
    @Builder.Default
    private List<Long> quizIds = new ArrayList<>();
    
    /**
     * 배틀 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleStatus status;
    
    /**
     * 호스트 점수
     */
    @Column(name = "host_score")
    @Builder.Default
    private Integer hostScore = 0;
    
    /**
     * 게스트 점수
     */
    @Column(name = "guest_score")
    @Builder.Default
    private Integer guestScore = 0;
    
    /**
     * 승자 ID
     */
    @Column(name = "winner_id")
    private Long winnerId;
    
    /**
     * 배틀 시작 시간
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 배틀 종료 시간
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 배틀 시작
     */
    public void startBattle() {
        if (this.status == BattleStatus.WAITING) {
            this.status = BattleStatus.IN_PROGRESS;
            this.startTime = LocalDateTime.now();
        }
    }
    
    /**
     * 배틀 종료
     */
    public void endBattle() {
        if (this.status == BattleStatus.IN_PROGRESS) {
            this.status = BattleStatus.COMPLETED;
            this.endTime = LocalDateTime.now();
            determineWinner();
        }
    }
    
    /**
     * 승자 결정
     */
    private void determineWinner() {
        if (hostScore > guestScore) {
            this.winnerId = hostUserId;
        } else if (guestScore > hostScore) {
            this.winnerId = guestUserId;
        } else {
            // 동점인 경우 무승부로 처리 (null)
            this.winnerId = null;
        }
    }
    
    /**
     * 호스트 점수 증가
     */
    public void incrementHostScore() {
        this.hostScore++;
    }
    
    /**
     * 게스트 점수 증가
     */
    public void incrementGuestScore() {
        this.guestScore++;
    }
}

