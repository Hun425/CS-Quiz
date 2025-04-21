package com.quizplatform.modules.user.domain;

import com.quizplatform.common.event.DomainEventPublisher;
import com.quizplatform.modules.quiz.domain.Achievement;
import com.quizplatform.modules.user.event.UserLevelUpEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 사용자 레벨 관리 엔티티 클래스
 *
 * <p>사용자의 레벨, 경험치, 업적 등 성장 관련 정보를 관리합니다.
 * 경험치 획득 및 레벨업 로직을 처리합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "user_levels")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class UserLevel {

    /**
     * 사용자 레벨 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 레벨 정보가 연결된 사용자
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 현재 레벨
     */
    private int level;

    /**
     * 현재 보유 경험치
     */
    private int currentExp;

    /**
     * 다음 레벨에 필요한 경험치
     */
    private int requiredExp;

    /**
     * 사용자가 획득한 업적 목록
     */
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_achievements",
            joinColumns = @JoinColumn(name = "user_level_id")
    )
    private Set<Achievement> achievements = new HashSet<>();

    /**
     * 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 경험치 획득 및 레벨업 처리
     *
     * @param exp 획득한 경험치
     */
    public void gainExp(int exp) {
        this.currentExp += exp;
        while (currentExp >= requiredExp) {
            levelUp();
        }
    }

    /**
     * 레벨업 처리
     *
     * <p>레벨을 증가시키고, 필요 경험치를 재계산하며, 레벨업 이벤트를 발행합니다.</p>
     */
    private void levelUp() {
        int oldLevel = level; // 기존 레벨 저장
        level++;
        currentExp -= requiredExp;
        requiredExp = calculateNextLevelExp();
        // 두 개의 인자(this, oldLevel)를 전달합니다.
        DomainEventPublisher.publishEvent(new UserLevelUpEvent(this, oldLevel));
    }

    /**
     * 사용자 레벨 생성자
     *
     * @param user 레벨 정보를 관리할 사용자
     */
    public UserLevel(User user) {
        this.user = user;
        this.level = 1; // 초기 레벨
        this.currentExp = 0; // 초기 경험치
        this.requiredExp = 100; // 다음 레벨에 필요한 초기 경험치
    }

    /**
     * 다음 레벨에 필요한 경험치 계산
     *
     * @return 필요 경험치
     */
    private int calculateNextLevelExp() {
        return (int) (requiredExp * 1.5); // 레벨당 1.5배씩 증가
    }
}