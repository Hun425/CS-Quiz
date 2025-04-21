package com.quizplatform.modules.user.domain;


import com.quizplatform.modules.quiz.domain.Achievement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 업적 획득 이력 엔티티 클래스
 * 
 * <p>사용자가 획득한 업적과 그 획득 시간을 기록합니다.
 * 업적 획득 이력은 사용자의 성취를 추적하고 표시하는 데 사용됩니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "user_achievement_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAchievementHistory {

    /**
     * 이력 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 업적을 획득한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 획득한 업적
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "achievement")
    private Achievement achievement;

    /**
     * 업적 이름 (레코드 보존용)
     */
    @Column(name = "achievement_name")
    private String achievementName;

    /**
     * 업적 획득 시간
     */
    @CreatedDate
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    /**
     * 업적 획득 이력 생성자
     * 
     * @param user 업적을 획득한 사용자
     * @param achievement 획득한 업적
     */
    public UserAchievementHistory(User user, Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
        this.achievementName = achievement.getName();
    }
}