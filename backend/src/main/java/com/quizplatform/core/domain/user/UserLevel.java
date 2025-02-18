package com.quizplatform.core.domain.user;

import com.quizplatform.core.domain.quiz.Achievement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_levels")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int level;
    private int currentExp;
    private int requiredExp;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_achievements",
            joinColumns = @JoinColumn(name = "user_level_id")
    )
    private Set<Achievement> achievements = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 경험치 획득 메서드
    public void gainExp(int exp) {
        this.currentExp += exp;
        while (currentExp >= requiredExp) {
            levelUp();
        }
    }

    // 레벨업 처리
    private void levelUp() {
        level++;
        currentExp -= requiredExp;
        requiredExp = calculateNextLevelExp();
        // 레벨업 이벤트 발행
        ApplicationEventPublisher.publishEvent(new UserLevelUpEvent(this));
    }

    // 다음 레벨 필요 경험치 계산
    private int calculateNextLevelExp() {
        return (int) (requiredExp * 1.5); // 레벨당 1.5배씩 증가
    }
}