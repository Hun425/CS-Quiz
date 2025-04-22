package com.quizplatform.user.application.service;

import com.quizplatform.user.application.port.in.GetUserAchievementHistoryQuery;
import com.quizplatform.user.application.port.in.GetUserLevelHistoryQuery;
import com.quizplatform.user.application.port.in.GetUserQuery;
import com.quizplatform.user.application.port.in.dto.UserAchievementHistoryDto;
import com.quizplatform.user.application.port.in.dto.UserLevelHistoryDto;
import com.quizplatform.user.application.port.out.LoadUserAchievementHistoryPort;
import com.quizplatform.user.application.port.out.LoadUserLevelHistoryPort;
import com.quizplatform.user.application.port.out.LoadUserPort;
import com.quizplatform.user.domain.model.User;
import com.quizplatform.user.domain.model.UserAchievementHistory;
import com.quizplatform.user.domain.model.UserLevelHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException; // For user not found

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 서비스는 readOnly 트랜잭션
public class UserQueryService implements GetUserQuery, GetUserLevelHistoryQuery, GetUserAchievementHistoryQuery {

    private final LoadUserPort loadUserPort;
    private final LoadUserLevelHistoryPort loadUserLevelHistoryPort;
    private final LoadUserAchievementHistoryPort loadUserAchievementHistoryPort;

    @Override
    public User getUserById(Long userId) {
        // Optional<User>를 처리하여 User 반환 또는 예외 발생
        return loadUserPort.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
    }

    @Override
    public Page<UserLevelHistoryDto> getUserLevelHistory(Long userId, Pageable pageable) {
        // Port를 통해 도메인 모델 Page 조회
        Page<UserLevelHistory> historyPage = loadUserLevelHistoryPort.findByUserId(userId, pageable);
        // Page<Domain> -> Page<DTO> 변환
        return historyPage.map(this::toLevelHistoryDto); // DTO 변환 메소드 사용
    }

    @Override
    public Page<UserAchievementHistoryDto> getUserAchievementHistory(Long userId, Pageable pageable) {
        // Port를 통해 도메인 모델 Page 조회
        Page<UserAchievementHistory> historyPage = loadUserAchievementHistoryPort.findByUserId(userId, pageable);
        // Page<Domain> -> Page<DTO> 변환
        return historyPage.map(this::toAchievementHistoryDto); // DTO 변환 메소드 사용
    }

    // --- DTO 변환 메소드 --- (별도 Mapper 클래스로 분리 가능)

    private UserLevelHistoryDto toLevelHistoryDto(UserLevelHistory domain) {
        return UserLevelHistoryDto.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .previousLevel(domain.getPreviousLevel())
                .level(domain.getLevel())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private UserAchievementHistoryDto toAchievementHistoryDto(UserAchievementHistory domain) {
        return UserAchievementHistoryDto.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .achievementName(domain.getAchievementName())
                .earnedAt(domain.getEarnedAt())
                .build();
    }
} 