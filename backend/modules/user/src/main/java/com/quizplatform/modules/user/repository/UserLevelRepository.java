package com.quizplatform.modules.user.repository;

import com.quizplatform.modules.user.domain.User;
import com.quizplatform.modules.user.domain.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 레벨 정보에 대한 데이터 액세스 인터페이스
 * 
 * @author 채기훈
 * @since JDK 17
 */
@Repository
public interface UserLevelRepository extends JpaRepository<UserLevel, Long> {
    
    /**
     * 사용자로 레벨 정보 조회
     * 
     * @param user 사용자 엔티티
     * @return 사용자 레벨 정보
     */
    Optional<UserLevel> findByUser(User user);
    
    /**
     * 사용자 ID로 레벨 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 레벨 정보
     */
    Optional<UserLevel> findByUserId(Long userId);
}