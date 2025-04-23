package com.quizplatform.modules.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * 레벨업 기록 클래스
 * 
 * <p>사용자의 레벨업 이벤트를 기록하는 DTO 클래스입니다.
 * 이전 레벨, 새 레벨, 발생 시간 등의 정보를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
@AllArgsConstructor
public class LevelUpRecord {
    /**
     * 기록 ID
     */
    private Long id;
    
    /**
     * 레벨업한 사용자 ID
     */
    private Long userId;
    
    /**
     * 이전 레벨
     */
    private Integer oldLevel;
    
    /**
     * 새 레벨
     */
    private Integer newLevel;
    
    /**
     * 레벨업 발생 시간
     */
    private ZonedDateTime occurredAt;
}