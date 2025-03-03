// src/api/battleApi.ts - 배틀 관련 API 연동
import axios from 'axios';
import { BattleRoomResponse, BattleRoomCreateRequest } from '../types/api';
import { getAuthHeader } from '../utils/auth';

const BASE_URL = 'http://localhost:8080/api';

const apiClient = axios.create({
    baseURL: BASE_URL,
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

export const battleApi = {
    // 배틀룸 생성
    createBattleRoom: async (request: BattleRoomCreateRequest) => {
        return apiClient.post<{ success: boolean, data: BattleRoomResponse }>('/battles', request);
    },

    // 배틀룸 조회
    getBattleRoom: async (roomId: number) => {
        return apiClient.get<{ success: boolean, data: BattleRoomResponse }>(`/battles/${roomId}`);
    },

    // 활성화된 배틀룸 목록 조회
    getActiveBattleRooms: async () => {
        return apiClient.get<{ success: boolean, data: BattleRoomResponse[] }>('/battles/active');
    },

    // 배틀룸 참가
    joinBattleRoom: async (roomId: number) => {
        return apiClient.post<{ success: boolean, data: BattleRoomResponse }>(`/battles/${roomId}/join`);
    },

    // 준비 상태 토글
    toggleReady: async (roomId: number) => {
        return apiClient.post<{ success: boolean, data: BattleRoomResponse }>(`/battles/${roomId}/ready`);
    },

    // 배틀룸 나가기
    leaveBattleRoom: async (roomId: number) => {
        return apiClient.post<{ success: boolean, data: BattleRoomResponse }>(`/battles/${roomId}/leave`);
    },

    // 내 활성 배틀룸 조회
    getMyActiveBattleRoom: async () => {
        return apiClient.get<{ success: boolean, data: BattleRoomResponse }>('/battles/my-active');
    }
};