// src/api/battleApi.ts - 배틀(대결) 관련 API 연동
import { api } from './client';
import {
    BattleRoomResponse,
    BattleRoomCreateRequest,
    CommonApiResponse
} from '../types/api';

export const battleApi = {
    // 배틀룸 생성
    createBattleRoom: async (request: BattleRoomCreateRequest) => {
        return api.post<CommonApiResponse<BattleRoomResponse>>('/battles', request);
    },

    // 배틀룸 조회
    getBattleRoom: async (roomId: number) => {
        return api.get<CommonApiResponse<BattleRoomResponse>>(`/battles/${roomId}`);
    },

    // 활성화된 배틀룸 목록 조회
    getActiveBattleRooms: async () => {
        return api.get<CommonApiResponse<BattleRoomResponse[]>>('/battles/active');
    },

    // 배틀룸 참가
    joinBattleRoom: async (roomId: number) => {
        return api.post<CommonApiResponse<BattleRoomResponse>>(`/battles/${roomId}/join`);
    },

    // 준비 상태 토글
    toggleReady: async (roomId: number) => {
        return api.post<CommonApiResponse<BattleRoomResponse>>(`/battles/${roomId}/ready`);
    },

    // 배틀룸 나가기
    leaveBattleRoom: async (roomId: number) => {
        return api.post<CommonApiResponse<BattleRoomResponse>>(`/battles/${roomId}/leave`);
    },

    // 내 활성 배틀룸 조회
    getMyActiveBattleRoom: async () => {
        return api.get<CommonApiResponse<BattleRoomResponse>>('/battles/my-active');
    },

    // 답변 제출
    submitAnswer: async (roomId: number, questionId: number, answerData: any) => {
        return api.post<CommonApiResponse<any>>(`/battles/${roomId}/answers`, {
            questionId,
            ...answerData
        });
    },

    // 배틀 결과 조회
    getBattleResult: async (roomId: number) => {
        return api.get<CommonApiResponse<any>>(`/battles/${roomId}/result`);
    }
};