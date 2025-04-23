package com.quizplatform.quiz.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 DTO
 * 
 * <p>다른 모듈에서 전달받은 사용자 정보를 담는 DTO 객체입니다.
 * User 모듈과의 의존성 분리를 위해 사용합니다.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    /**
     * 사용자 ID
     */
    private String id;
    
    /**
     * 사용자명
     */
    private String username;
    
    /**
     * 이메일
     */
    private String email;
    
    /**
     * 레벨
     */
    private int level;
    
    /**
     * 경험치
     */
    private int experience;
    
    /**
     * 역할
     */
    private String role;
} 