package com.quizplatform.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * 사용자 상태를 나타내는 도메인 모델입니다.
 * 이전에는 UserLevel이라는 이름으로 사용되었으나, 
 * UserLevel은 레벨 정의를 위한 모델로 변경되었습니다.
 */
@Getter
public class UserStatus {

    private final Long id;
    private final Long userId;
    private final int currentLevel;
    private final int currentExperience;
    private final int totalExperience;
    private final int requiredExperience;
    private final ZonedDateTime lastLevelUpDate;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    @Builder
    public UserStatus(Long id, Long userId, int currentLevel, int currentExperience, 
                     int totalExperience, int requiredExperience, 
                     ZonedDateTime lastLevelUpDate, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.currentLevel = currentLevel;
        this.currentExperience = currentExperience;
        this.totalExperience = totalExperience;
        this.requiredExperience = requiredExperience;
        this.lastLevelUpDate = lastLevelUpDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 경험치를 추가하고 레벨업 여부를 반환합니다.
     * 실제 레벨업 처리는 애플리케이션 서비스에서 해야 합니다.
     *
     * @param expToAdd 추가할 경험치
     * @param requiredExpForNextLevel 다음 레벨에 필요한 경험치
     * @return 레벨업 여부
     */
    public boolean addExperience(int expToAdd, int requiredExpForNextLevel) {
        // 도메인 로직은 실제 구현에 맞게 조정 필요
        if (expToAdd <= 0) {
            return false;
        }
        
        int newCurrentExp = this.currentExperience + expToAdd;
        boolean leveledUp = newCurrentExp >= this.requiredExperience;
        
        // 실제 레벨업 및 상태 변경은 애플리케이션 서비스에서 처리
        return leveledUp;
    }
    
    /**
     * 새로운 상태로 업데이트된 UserStatus를 반환합니다(불변 객체 패턴).
     */
    public UserStatus withUpdatedStatus(int newLevel, int newCurrentExp, int newTotalExp, int newRequiredExp) {
        return UserStatus.builder()
                .id(this.id)
                .userId(this.userId)
                .currentLevel(newLevel)
                .currentExperience(newCurrentExp)
                .totalExperience(newTotalExp)
                .requiredExperience(newRequiredExp)
                .lastLevelUpDate(newLevel > this.currentLevel ? ZonedDateTime.now() : this.lastLevelUpDate)
                .createdAt(this.createdAt)
                .updatedAt(ZonedDateTime.now())
                .build();
    }
}