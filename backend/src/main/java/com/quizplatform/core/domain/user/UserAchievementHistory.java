package com.quizplatform.core.domain.user;

import com.quizplatform.core.domain.quiz.Achievement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievement_history")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAchievementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement")
    private Achievement achievement;

    @Column(name = "achievement_name")
    private String achievementName;

    @CreatedDate
    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    public UserAchievementHistory(User user, Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
        this.achievementName = achievement.getName();
    }
}