// src/api/userApi.ts
import { api } from './client';
import {
    CommonApiResponse,
    PageResponse
} from '../types/api';
import {
    UserProfile,
    UserStatistics,
    RecentActivity,
    Achievement,
    TopicPerformance
} from '../types/user';

export const userApi = {
    // 사용자 프로필 정보 가져오기
    getUserProfile: (userId?: number) => {
        const endpoint = userId ? `/api/users/${userId}/profile` : '/api/users/me/profile';
        return api.get<CommonApiResponse<UserProfile>>(endpoint);
    },

    // 사용자 통계 가져오기
    getUserStatistics: (userId?: number) => {
        const endpoint = userId ? `/api/users/${userId}/statistics` : '/api/users/me/statistics';
        return api.get<CommonApiResponse<UserStatistics>>(endpoint);
    },

    // 최근 활동 가져오기
    getRecentActivities: (userId?: number, limit = 10) => {
        const endpoint = userId ? `/api/users/${userId}/recent-activities` : '/api/users/me/recent-activities';
        return api.get<CommonApiResponse<RecentActivity[]>>(endpoint, {
            params: { limit }
        });
    },

    // 업적 가져오기
    getAchievements: (userId?: number) => {
        const endpoint = userId ? `/api/users/${userId}/achievements` : '/api/users/me/achievements';
        return api.get<CommonApiResponse<Achievement[]>>(endpoint);
    },

    // 주제별 성과 가져오기
    getTopicPerformance: (userId?: number) => {
        const endpoint = userId ? `/api/users/${userId}/topic-performance` : '/api/users/me/topic-performance';
        return api.get<CommonApiResponse<TopicPerformance[]>>(endpoint);
    },

    // 프로필 정보 업데이트
    updateProfile: (data: Partial<UserProfile>) => {
        return api.put<CommonApiResponse<UserProfile>>('/api/users/me/profile', data);
    },
};