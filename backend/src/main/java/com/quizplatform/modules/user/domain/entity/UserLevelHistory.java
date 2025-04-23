package com.quizplatform.modules.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 레벨 변경 이력 엔티티 클래스
 * 
 * <p>사용자의 레벨 변경 내역을 기록하여 성장 과정을 추적합니다.
 * 변경 전후 레벨과 변경 시간을 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "user_level_history", schema = "user_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevelHistory {

    /**
     * 이력 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 레벨이 변경된 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 변경 전 레벨
     */
    @Column(name = "previous_level", nullable = false)
    private int previousLevel;

    /**
     * 변경 후 레벨
     */
    @Column(name = "level", nullable = false)
    private int level;

    /**
     * 레벨 변경 시간
     */
    @CreatedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 레벨 변경 이력 생성자
     * 
     * @param user 레벨이 변경된 사용자
     * @param previousLevel 변경 전 레벨
     * @param level 변경 후 레벨
     */
    public UserLevelHistory(User user, int previousLevel, int level) {
        this.user = user;
        this.previousLevel = previousLevel;
        this.level = level;
    }
}