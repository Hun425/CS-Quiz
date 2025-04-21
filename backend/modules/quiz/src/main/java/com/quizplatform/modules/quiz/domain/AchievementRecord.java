package com.quizplatform.modules.quiz.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * 사용자 업적 달성 기록 클래스
 * 
 * <p>사용자가 달성한 업적에 대한 기록을 관리합니다.
 * 업적 ID, 이름, 달성 시간 등의 정보를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Getter
@AllArgsConstructor
public class AchievementRecord {
    /**
     * 기록 ID
     */
    private Long id;
    
    /**
     * 업적을 달성한 사용자 ID
     */
    private Long userId;
    
    /**
     * 달성한 업적 ID
     */
    private Long achievementId;
    
    /**
     * 달성한 업적 이름
     */
    private String achievementName;
    
    /**
     * 업적 달성 시간
     */
    private ZonedDateTime earnedAt;
}