package com.quizplatform.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 레벨 변경 이력 엔티티 클래스
 * 
 * <p>사용자의 레벨 변경 기록을 관리합니다. 레벨업 시간과 변경 전후 레벨 정보를 포함합니다.</p>
 */
@Entity
@Table(name = "user_level_histories", schema = "user_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevelHistory {

    /**
     * 레벨 변경 이력 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 레벨이 변경된 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 이전 레벨
     */
    @Column(name = "previous_level", nullable = false)
    private int previousLevel;

    /**
     * 새 레벨
     */
    @Column(name = "new_level", nullable = false)
    private int newLevel;

    /**
     * 획득한 보너스 포인트
     */
    @Column(name = "bonus_points")
    private int bonusPoints;

    /**
     * 레벨 변경 이유
     */
    @Column(name = "level_up_reason")
    private String levelUpReason;

    /**
     * 레벨 변경 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 레벨 변경 이력 생성자
     * 
     * @param user 사용자
     * @param previousLevel 이전 레벨
     * @param newLevel 새 레벨
     * @param bonusPoints 획득 보너스 포인트
     * @param levelUpReason 레벨업 이유
     */
    @Builder
    public UserLevelHistory(User user, int previousLevel, int newLevel, int bonusPoints, String levelUpReason) {
        this.user = user;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.bonusPoints = bonusPoints != 0 ? bonusPoints : 0;
        this.levelUpReason = levelUpReason;
    }
} 