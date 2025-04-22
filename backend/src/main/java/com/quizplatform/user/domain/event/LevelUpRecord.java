package com.quizplatform.user.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * 레벨업 기록 클래스 (이벤트 페이로드/DTO)
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
     * 기록 ID (필요시 사용, 이벤트 자체의 ID일 수도 있음)
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