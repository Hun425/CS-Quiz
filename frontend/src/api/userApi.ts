// src/api/userApi.ts - 사용자 관련 API 연동
import axios from 'axios';
import { UserProfile, UserStatistics, TopicPerformance, Achievement, RecentActivity } from '../types/user';
import { getAuthHeader } from '../utils/auth';
import config from '../config/environment';

const apiClient = axios.create({
    baseURL: config.apiBaseUrl,
    headers: {
        'Content-Type': 'application/json',
    },
});

apiClient.interceptors.request.use(
    (config) => {
        const authHeader = getAuthHeader();
        if (authHeader) {
            config.headers = {
                ...config.headers,
                ...authHeader,
            };
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const userApi = {
    // 사용자 프로필 조회
    getUserProfile: async (userId?: number) => {
        const endpoint = userId ? `/users/${userId}/profile` : '/users/me/profile';
        return apiClient.get<{ success: boolean, data: UserProfile }>(endpoint);
    },

    // 사용자 통계 조회
    getUserStatistics: async (userId?: number) => {
        const endpoint = userId ? `/users/${userId}/statistics` : '/users/me/statistics';
        return apiClient.get<{ success: boolean, data: UserStatistics }>(endpoint);
    },

    // 최근 활동 조회
    getRecentActivities: async (userId?: number, limit: number = 10) => {
        const endpoint = userId
            ? `/users/${userId}/recent-activities?limit=${limit}`
            : `/users/me/recent-activities?limit=${limit}`;
        return apiClient.get<{ success: boolean, data: RecentActivity[] }>(endpoint);
    },

    // 업적 조회
    getAchievements: async (userId?: number) => {
        const endpoint = userId ? `/users/${userId}/achievements` : '/users/me/achievements';
        return apiClient.get<{ success: boolean, data: Achievement[] }>(endpoint);
    },

    // 주제별 성과 조회
    getTopicPerformance: async (userId?: number) => {
        const endpoint = userId ? `/users/${userId}/topic-performance` : '/users/me/topic-performance';
        return apiClient.get<{ success: boolean, data: TopicPerformance[] }>(endpoint);
    },

    // 프로필 정보 업데이트
    updateProfile: async (userData: { username?: string, profileImage?: string }) => {
        return apiClient.put<{ success: boolean, data: UserProfile }>('/users/me/profile', userData);
    }
};