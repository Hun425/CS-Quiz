package com.quizplatform.user.adapter.in.web;

import com.quizplatform.user.application.port.in.*;
import com.quizplatform.user.application.port.in.command.UpdateUserProfileCommand;
import com.quizplatform.user.application.port.in.dto.UserAchievementHistoryDto;
import com.quizplatform.user.application.port.in.dto.UserLevelHistoryDto;
import com.quizplatform.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserQuery getUserQuery;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final GetUserLevelHistoryQuery getUserLevelHistoryQuery;
    private final GetUserAchievementHistoryQuery getUserAchievementHistoryQuery;
    // private final UpdateUserLevelUseCase updateUserLevelUseCase; // 필요시 주입

    /**
     * 사용자 정보 조회 API
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보 (민감 정보 제외 필요)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        User user = getUserQuery.getUserById(userId);
        // 민감 정보(password 등) 제외하고 DTO로 변환하여 반환
        UserProfileResponse response = UserProfileResponse.fromDomain(user);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 프로필 수정 API
     * @param userId 수정할 사용자 ID
     * @param command 수정할 프로필 정보 (nickname, email 등)
     * @return 업데이트된 사용자 정보 (민감 정보 제외)
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@PathVariable Long userId,
                                                             @Valid @RequestBody UpdateUserProfileCommand command) {
        // URL의 userId와 command의 userId가 일치하는지 확인하는 로직 추가 가능 (보안)
        if (!userId.equals(command.getUserId())) {
            // 권한 검사 또는 잘못된 요청 처리
            return ResponseEntity.badRequest().build(); // 간단 예시
        }
        User updatedUser = updateUserProfileUseCase.updateUserProfile(command);
        UserProfileResponse response = UserProfileResponse.fromDomain(updatedUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 레벨 변경 이력 조회 API
     * @param userId 조회할 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 레벨 변경 이력
     */
    @GetMapping("/{userId}/level-history")
    public ResponseEntity<Page<UserLevelHistoryDto>> getUserLevelHistory(@PathVariable Long userId, Pageable pageable) {
        Page<UserLevelHistoryDto> historyPage = getUserLevelHistoryQuery.getUserLevelHistory(userId, pageable);
        return ResponseEntity.ok(historyPage);
    }

    /**
     * 사용자 업적 획득 이력 조회 API
     * @param userId 조회할 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 업적 획득 이력
     */
    @GetMapping("/{userId}/achievement-history")
    public ResponseEntity<Page<UserAchievementHistoryDto>> getUserAchievementHistory(@PathVariable Long userId, Pageable pageable) {
        Page<UserAchievementHistoryDto> historyPage = getUserAchievementHistoryQuery.getUserAchievementHistory(userId, pageable);
        return ResponseEntity.ok(historyPage);
    }


    // --- Helper DTO for Response ---
    // 별도 파일 분리 권장
    @Getter
    private static class UserProfileResponse {
        private final Long userId;
        private final String username;
        private final String nickname;
        private final String email;
        private final String profileImage;
        private final int level;
        private final int experience;
        private final int requiredExperience;
        private final int totalPoints;

        private UserProfileResponse(User user) {
            this.userId = user.getId();
            this.username = user.getUsername();
            this.nickname = user.getNickname();
            this.email = user.getEmail(); // 민감 정보 노출 주의, 필요시 마스킹
            this.profileImage = user.getProfileImage();
            this.level = user.getLevel();
            this.experience = user.getExperience();
            this.requiredExperience = user.getRequiredExperience();
            this.totalPoints = user.getTotalPoints();
        }

        public static UserProfileResponse fromDomain(User user) {
            return new UserProfileResponse(user);
        }
    }
} 