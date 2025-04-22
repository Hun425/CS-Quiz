package com.quizplatform.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * UserLevel 도메인 모델
 */
@Getter
public class UserLevel {

    private Long id;
    private Long userId; // User ID 참조
    private int level;
    private int currentExp;
    private int requiredExp;
    private Set<String> achievements; // 업적 이름 목록
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public UserLevel(Long id, Long userId, int level, int currentExp, int requiredExp, Set<String> achievements, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.level = (level == 0) ? 1 : level;
        this.currentExp = currentExp;
        this.requiredExp = (requiredExp == 0) ? 100 : requiredExp;
        this.achievements = achievements != null ? achievements : new java.util.HashSet<>(); // Null 체크 및 초기화
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 경험치 획득 및 레벨업 처리
     *
     * @param exp 획득한 경험치
     * @return 레벨업 발생 여부
     */
    public boolean gainExp(int exp) {
        this.currentExp += exp;
        boolean leveledUp = false;
        while (currentExp >= requiredExp) {
            levelUp();
            leveledUp = true;
        }
        this.updatedAt = LocalDateTime.now(); // 상태 변경 시각 업데이트
        return leveledUp;
    }

    /**
     * 레벨업 처리
     */
    private void levelUp() {
        level++;
        currentExp -= requiredExp;
        requiredExp = calculateNextLevelExp();
    }

    /**
     * 다음 레벨에 필요한 경험치 계산
     *
     * @return 필요 경험치
     */
    private int calculateNextLevelExp() {
        return (int) (requiredExp * 1.5);
    }

    /**
     * 업적 추가
     * @param achievementName 추가할 업적 이름
     */
    public void addAchievement(String achievementName) {
        if (achievementName != null && !achievementName.trim().isEmpty()) {
            if (this.achievements == null) { // Null 체크 추가
                this.achievements = new java.util.HashSet<>();
            }
            this.achievements.add(achievementName);
            this.updatedAt = LocalDateTime.now();
        }
    }
} 