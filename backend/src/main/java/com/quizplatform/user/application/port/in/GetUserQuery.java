package com.quizplatform.user.application.port.in;

import com.quizplatform.user.domain.model.User;

/**
 * 사용자 정보 조회 유스케이스 인터페이스 (Query)
 */
public interface GetUserQuery {

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 조회된 User 도메인 모델. 찾지 못하면 예외 발생 또는 Optional<User> 반환 (설계에 따라 다름)
     */
    User getUserById(Long userId);
    
    // 필요에 따라 다른 조회 메소드 추가 가능 (예: getUserByUsername)
} 