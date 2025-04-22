package com.quizplatform.user.application.port.in;

import com.quizplatform.user.application.port.in.dto.UserAchievementHistoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 사용자 업적 획득 이력 조회 유스케이스 인터페이스 (Query)
 */
public interface GetUserAchievementHistoryQuery {

    /**
     * 특정 사용자의 업적 획득 이력을 페이징하여 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 등)
     * @return 페이징된 업적 획득 이력 DTO 목록
     */
    Page<UserAchievementHistoryDto> getUserAchievementHistory(Long userId, Pageable pageable);
} 