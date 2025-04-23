package com.quizplatform.quiz.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 도메인 모델
 * 
 * <p>다른 모듈(User 모듈)에서 수신한 사용자 정보를 나타냅니다.
 * 이 모델은 퀴즈 모듈 내에서 사용자 정보를 참조하기 위한 목적으로만 사용됩니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 사용자 ID
     */
    private Long id;
    
    /**
     * 사용자명
     */
    private String username;
    
    /**
     * 이메일
     */
    private String email;
    
    /**
     * 사용자 레벨
     */
    private Integer level;
    
    /**
     * 경험치
     */
    private Integer experience;
    
    /**
     * 포인트
     */
    private Integer points;
} 