package com.quizplatform.quiz.adapter.out.persistence.cache;

import com.quizplatform.quiz.domain.dto.UserDTO;

/**
 * 사용자 캐시 서비스 인터페이스
 * 
 * <p>Quiz 모듈에서 필요한 사용자 정보를 캐싱하는 기능을 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface UserCacheService {
    
    /**
     * 사용자 정보 캐싱
     * 
     * @param userDTO 캐싱할 사용자 정보
     */
    void cacheUserInfo(UserDTO userDTO);
    
    /**
     * 사용자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 캐싱된 사용자 정보
     */
    UserDTO getUserInfo(String userId);
    
    /**
     * 사용자 정보 삭제
     * 
     * @param userId 사용자 ID
     * @return 삭제 성공 여부
     */
    boolean removeUserInfo(String userId);
    
    /**
     * 모든 사용자 정보 조회
     * 
     * @return 모든 사용자 정보
     */
    Iterable<UserDTO> getAllUsers();
} 