package com.quizplatform.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * 사용자 레벨 히스토리 엔티티
 * 
 * <p>사용자의 레벨 변경 이력을 저장하는 엔티티입니다.
 * 언제 어떤 레벨로 변경되었는지 기록합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Entity
@Table(name = "user_level_history", schema = "user_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevelHistory {
    
    /**
     * 히스토리 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 레벨업한 사용자
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
     * 변경된 레벨
     */
    @Column(nullable = false)
    private int level;
    
    /**
     * 레벨 변경 시각
     */
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    /**
     * 생성자
     * 
     * @param user 사용자
     * @param previousLevel 이전 레벨
     * @param level 새 레벨
     */
    public UserLevelHistory(User user, int previousLevel, int level) {
        this.user = user;
        this.previousLevel = previousLevel;
        this.level = level;
        this.updatedAt = ZonedDateTime.now();
    }
} 