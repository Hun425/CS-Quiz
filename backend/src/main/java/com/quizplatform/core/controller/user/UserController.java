package com.quizplatform.core.controller.user;


import com.quizplatform.core.dto.common.CommonApiResponse;
import com.quizplatform.core.dto.user.*;
import com.quizplatform.core.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 프로필 정보 가져오기
    @GetMapping("/{userId}/profile")
    public CommonApiResponse<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        return CommonApiResponse.success(userService.getUserProfile(userId));
    }

    @GetMapping("/me/profile")
    public CommonApiResponse<UserProfileDto> getMyProfile(@AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getUserProfile(userId));
    }

    // 사용자 통계 가져오기
    @GetMapping("/{userId}/statistics")
    public CommonApiResponse<UserStatisticsDto> getUserStatistics(@PathVariable Long userId) {
        return CommonApiResponse.success(userService.getUserStatistics(userId));
    }

    @GetMapping("/me/statistics")
    public CommonApiResponse<UserStatisticsDto> getMyStatistics(@AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getUserStatistics(userId));
    }

    // 최근 활동 가져오기
    @GetMapping("/{userId}/recent-activities")
    public CommonApiResponse<List<RecentActivityDto>> getRecentActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        return CommonApiResponse.success(userService.getRecentActivities(userId, limit));
    }

    @GetMapping("/me/recent-activities")
    public CommonApiResponse<List<RecentActivityDto>> getMyRecentActivities(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getRecentActivities(userId, limit));
    }

    // 업적 가져오기
    @GetMapping("/{userId}/achievements")
    public CommonApiResponse<List<AchievementDto>> getAchievements(@PathVariable Long userId) {
        return CommonApiResponse.success(userService.getAchievements(userId));
    }

    @GetMapping("/me/achievements")
    public CommonApiResponse<List<AchievementDto>> getMyAchievements(@AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getAchievements(userId));
    }

    // 주제별 성과 가져오기
    @GetMapping("/{userId}/topic-performance")
    public CommonApiResponse<List<TopicPerformanceDto>> getTopicPerformance(@PathVariable Long userId) {
        return CommonApiResponse.success(userService.getTopicPerformance(userId));
    }

    @GetMapping("/me/topic-performance")
    public CommonApiResponse<List<TopicPerformanceDto>> getMyTopicPerformance(@AuthenticationPrincipal OAuth2User principal) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.getTopicPerformance(userId));
    }

    // 프로필 정보 업데이트
    @PutMapping("/me/profile")
    public CommonApiResponse<UserProfileDto> updateProfile(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody UserProfileUpdateRequest request) {
        Long userId = Long.valueOf(principal.getName());
        return CommonApiResponse.success(userService.updateProfile(userId, request));
    }
}