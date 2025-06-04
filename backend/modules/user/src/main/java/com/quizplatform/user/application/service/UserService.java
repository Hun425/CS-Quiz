package com.quizplatform.user.application.service;

import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 서비스 인터페이스
 * 
 * <p>사용자 도메인의 핵심 비즈니스 로직을 정의한 인터페이스입니다.
 * 사용자 관리, 레벨업, 포인트 관리 등의 기능을 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface UserService {
    
    /**
     * 사용자 생성
     * 
     * @param user 생성할 사용자 정보
     * @return 생성된 사용자
     */
    User createUser(User user);
    
    /**
     * ID로 사용자 조회
     * 
     * @param id 사용자 ID
     * @return 사용자 Optional
     */
    Optional<User> findById(Long id);
    
    /**
     * 사용자명으로 사용자 조회
     * 
     * @param username 사용자명
     * @return 사용자 Optional
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 이메일로 사용자 조회
     * 
     * @param email 이메일
     * @return 사용자 Optional
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 모든 사용자 조회
     * 
     * @return 사용자 목록
     */
    List<User> findAllUsers();
    
    /**
     * 사용자에게 경험치 부여
     * 
     * @param userId 사용자 ID
     * @param experience 경험치
     * @return 레벨업 여부
     */
    boolean giveExperience(Long userId, int experience);
    
    /**
     * 사용자에게 포인트 부여
     * 
     * @param userId 사용자 ID
     * @param points 포인트
     */
    void givePoints(Long userId, int points);
    
    /**
     * 사용자 프로필 업데이트
     * 
     * @param userId 사용자 ID
     * @param username 새 사용자명 (변경하지 않을 경우 null)
     * @param profileImage 새 프로필 이미지 URL (변경하지 않을 경우 null)
     * @return 업데이트된 사용자
     */
    User updateProfile(Long userId, String username, String profileImage);
    
    /**
     * 사용자 계정 활성화/비활성화 토글
     * 
     * @param userId 사용자 ID
     * @return 업데이트된 사용자
     */
    User toggleActive(Long userId);
    
    /**
     * 사용자 권한 변경
     * 
     * @param userId 사용자 ID
     * @param role 새 권한
     * @return 업데이트된 사용자
     */
    User updateRole(Long userId, UserRole role);
} 