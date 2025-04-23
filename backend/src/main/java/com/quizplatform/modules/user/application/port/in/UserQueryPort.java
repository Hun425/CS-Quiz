package com.quizplatform.modules.user.application.port.in;

import java.util.Optional;

/**
 * 외부 모듈에서 사용자 정보를 동기적으로 조회하기 위한 Inbound Port 인터페이스
 */
public interface UserQueryPort {

    /**
     * 사용자 ID로 기본적인 사용자 정보를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보 DTO (UserInfo), 사용자가 없을 경우 Optional.empty()
     */
    Optional<UserInfo> getUserInfoById(Long userId);

    // 필요에 따라 다른 조회 메소드 추가 가능 (예: 여러 사용자 정보 조회)
    // List<UserInfo> getUserInfoByIds(List<Long> userIds);
} 