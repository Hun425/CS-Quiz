package com.quizplatform.core.controller.user;

import com.quizplatform.core.config.security.UserPrincipal;
import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.user.*;
import com.quizplatform.core.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 프로필, 통계, 업적 등 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 프로필 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<CommonApiResponse<UserProfileDto>> getUserProfile(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(CommonApiResponse.success(profile));
    }

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/profile")
    public ResponseEntity<CommonApiResponse<UserProfileDto>> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(CommonApiResponse.success(profile));
    }

    @Operation(summary = "사용자 통계 조회", description = "특정 사용자의 퀴즈 참여 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<CommonApiResponse<UserStatisticsDto>> getUserStatistics(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        UserStatisticsDto statistics = userService.getUserStatistics(userId);
        return ResponseEntity.ok(CommonApiResponse.success(statistics));
    }

    @Operation(summary = "내 통계 조회", description = "현재 로그인한 사용자의 퀴즈 참여 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/statistics")
    public ResponseEntity<CommonApiResponse<UserStatisticsDto>> getMyStatistics(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();
        UserStatisticsDto statistics = userService.getUserStatistics(userId);
        return ResponseEntity.ok(CommonApiResponse.success(statistics));
    }

    @Operation(summary = "사용자 최근 활동 조회", description = "특정 사용자의 최근 활동 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/recent-activities")
    public ResponseEntity<CommonApiResponse<List<RecentActivityDto>>> getRecentActivities(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "조회할 최대 활동 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<RecentActivityDto> activities = userService.getRecentActivities(userId, limit);
        return ResponseEntity.ok(CommonApiResponse.success(activities));
    }

    @Operation(summary = "내 최근 활동 조회", description = "현재 로그인한 사용자의 최근 활동 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/recent-activities")
    public ResponseEntity<CommonApiResponse<List<RecentActivityDto>>> getMyRecentActivities(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "조회할 최대 활동 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = principal.getId();
        List<RecentActivityDto> activities = userService.getRecentActivities(userId, limit);
        return ResponseEntity.ok(CommonApiResponse.success(activities));
    }

    @Operation(summary = "사용자 업적 조회", description = "특정 사용자가 획득한 업적 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/achievements")
    public ResponseEntity<CommonApiResponse<List<AchievementDto>>> getAchievements(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        List<AchievementDto> achievements = userService.getAchievements(userId);
        return ResponseEntity.ok(CommonApiResponse.success(achievements));
    }

    @Operation(summary = "내 업적 조회", description = "현재 로그인한 사용자가 획득한 업적 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/achievements")
    public ResponseEntity<CommonApiResponse<List<AchievementDto>>> getMyAchievements(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();
        List<AchievementDto> achievements = userService.getAchievements(userId);
        return ResponseEntity.ok(CommonApiResponse.success(achievements));
    }

    @Operation(summary = "사용자 주제별 성과 조회", description = "특정 사용자의 태그(주제)별 퀴즈 성과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주제별 성과 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/topic-performance")
    public ResponseEntity<CommonApiResponse<List<TopicPerformanceDto>>> getTopicPerformance(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        List<TopicPerformanceDto> topicPerformance = userService.getTopicPerformance(userId);
        return ResponseEntity.ok(CommonApiResponse.success(topicPerformance));
    }

    @Operation(summary = "내 주제별 성과 조회", description = "현재 로그인한 사용자의 태그(주제)별 퀴즈 성과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주제별 성과 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/topic-performance")
    public ResponseEntity<CommonApiResponse<List<TopicPerformanceDto>>> getMyTopicPerformance(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getId();
        List<TopicPerformanceDto> topicPerformance = userService.getTopicPerformance(userId);
        return ResponseEntity.ok(CommonApiResponse.success(topicPerformance));
    }

    @Operation(summary = "프로필 정보 업데이트", description = "현재 로그인한 사용자의 프로필 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PutMapping("/me/profile")
    public ResponseEntity<CommonApiResponse<UserProfileDto>> updateProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "업데이트할 프로필 정보", required = true)
            @RequestBody UserProfileUpdateRequest request) {
        Long userId = principal.getId();
        UserProfileDto updatedProfile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(CommonApiResponse.success(updatedProfile));
    }
}