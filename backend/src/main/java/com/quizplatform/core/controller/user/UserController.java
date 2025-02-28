package com.quizplatform.core.controller.user;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    public CommonApiResponse<UserProfileDto> getUserProfile(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        return CommonApiResponse.success(userService.getUserProfile(userId));
    }

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/profile")
    public CommonApiResponse<UserProfileDto> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getUserProfile(userId));
    }

    @Operation(summary = "사용자 통계 조회", description = "특정 사용자의 퀴즈 참여 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/statistics")
    public CommonApiResponse<UserStatisticsDto> getUserStatistics(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        return CommonApiResponse.success(userService.getUserStatistics(userId));
    }

    @Operation(summary = "내 통계 조회", description = "현재 로그인한 사용자의 퀴즈 참여 통계를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/statistics")
    public CommonApiResponse<UserStatisticsDto> getMyStatistics(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getUserStatistics(userId));
    }

    @Operation(summary = "사용자 최근 활동 조회", description = "특정 사용자의 최근 활동 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/recent-activities")
    public CommonApiResponse<List<RecentActivityDto>> getRecentActivities(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "조회할 최대 활동 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return CommonApiResponse.success(userService.getRecentActivities(userId, limit));
    }

    @Operation(summary = "내 최근 활동 조회", description = "현재 로그인한 사용자의 최근 활동 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활동 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/recent-activities")
    public CommonApiResponse<List<RecentActivityDto>> getMyRecentActivities(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Parameter(description = "조회할 최대 활동 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getRecentActivities(userId, limit));
    }

    @Operation(summary = "사용자 업적 조회", description = "특정 사용자가 획득한 업적 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/achievements")
    public CommonApiResponse<List<AchievementDto>> getAchievements(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        return CommonApiResponse.success(userService.getAchievements(userId));
    }

    @Operation(summary = "내 업적 조회", description = "현재 로그인한 사용자가 획득한 업적 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/achievements")
    public CommonApiResponse<List<AchievementDto>> getMyAchievements(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getAchievements(userId));
    }

    @Operation(summary = "사용자 주제별 성과 조회", description = "특정 사용자의 태그(주제)별 퀴즈 성과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주제별 성과 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/topic-performance")
    public CommonApiResponse<List<TopicPerformanceDto>> getTopicPerformance(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        return CommonApiResponse.success(userService.getTopicPerformance(userId));
    }

    @Operation(summary = "내 주제별 성과 조회", description = "현재 로그인한 사용자의 태그(주제)별 퀴즈 성과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주제별 성과 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me/topic-performance")
    public CommonApiResponse<List<TopicPerformanceDto>> getMyTopicPerformance(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getTopicPerformance(userId));
    }

    @Operation(summary = "프로필 정보 업데이트", description = "현재 로그인한 사용자의 프로필 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = CommonApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PutMapping("/me/profile")
    public CommonApiResponse<UserProfileDto> updateProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal,
            @Parameter(description = "업데이트할 프로필 정보", required = true)
            @RequestBody UserProfileUpdateRequest request) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.updateProfile(userId, request));
    }
}