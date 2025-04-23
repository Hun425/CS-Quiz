package com.quizplatform.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 레벨 엔티티 클래스
 * 
 * <p>사용자의 레벨 정보를 관리합니다. 각 레벨에 필요한 경험치와
 * 레벨업 시 부여되는 혜택 등을 포함합니다.</p>
 */
@Entity
@Table(name = "user_levels", schema = "user_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevel {

    /**
     * 레벨 ID (곧 레벨 값과 동일)
     */
    @Id
    private Integer level;

    /**
     * 레벨 이름
     */
    @Column(nullable = false)
    private String name;

    /**
     * 레벨에 도달하기 위한 필요 경험치
     */
    @Column(name = "required_experience", nullable = false)
    private int requiredExperience;

    /**
     * 레벨 업 시 보너스 포인트
     */
    @Column(name = "level_up_bonus", nullable = false)
    private int levelUpBonus;

    /**
     * 레벨 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 레벨 아이콘 URL
     */
    @Column(name = "icon_url")
    private String iconUrl;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 사용자 레벨 생성자
     * 
     * @param level 레벨 값
     * @param name 레벨 이름
     * @param requiredExperience 필요 경험치
     * @param levelUpBonus 레벨업 보너스
     * @param description 레벨 설명
     * @param iconUrl 레벨 아이콘 URL
     */
    @Builder
    public UserLevel(Integer level, String name, int requiredExperience, int levelUpBonus, String description, String iconUrl) {
        this.level = level;
        this.name = name;
        this.requiredExperience = requiredExperience;
        this.levelUpBonus = levelUpBonus;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    /**
     * 레벨 정보 업데이트
     * 
     * @param name 새 레벨 이름
     * @param requiredExperience 새 필요 경험치
     * @param levelUpBonus 새 레벨업 보너스
     * @param description 새 레벨 설명
     * @param iconUrl 새 레벨 아이콘 URL
     */
    public void update(String name, int requiredExperience, int levelUpBonus, String description, String iconUrl) {
        this.name = name;
        this.requiredExperience = requiredExperience;
        this.levelUpBonus = levelUpBonus;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    /**
     * 다음 레벨까지 필요한 경험치 계산
     * 
     * @param currentExp 현재 경험치
     * @return 다음 레벨까지 필요한 경험치
     */
    public int calculateExpToNextLevel(int currentExp) {
        return Math.max(0, requiredExperience - currentExp);
    }

    /**
     * 레벨업 여부 확인
     * 
     * @param currentExp 현재 경험치
     * @return 레벨업 가능 여부
     */
    public boolean isLevelUpAvailable(int currentExp) {
        return currentExp >= requiredExperience;
    }
} 