// src/api/quizApi.ts
import { api } from './client';
import { CommonApiResponse, PageResponse, QuizSummaryResponse, QuizDetailResponse, QuizResponse, QuizCreateRequest } from '../types/api';

// API 함수들
export const quizApi = {
    // 퀴즈 목록 조회
    searchQuizzes: (params: any, page = 0, size = 10) => {
        return api.get<CommonApiResponse<PageResponse<QuizSummaryResponse>>>('/api/quizzes/search', {
            params: {
                ...params,
                page,
                size,
            },
        });
    },

    // 퀴즈 상세 조회
    getQuiz: (quizId: number) => {
        return api.get<CommonApiResponse<QuizDetailResponse>>(`/api/quizzes/${quizId}`);
    },

    // 퀴즈 생성
    createQuiz: (quiz: QuizCreateRequest) => {
        return api.post<CommonApiResponse<QuizResponse>>('/api/quizzes', quiz);
    },

    // 퀴즈 수정
    updateQuiz: (quizId: number, quiz: QuizCreateRequest) => {
        return api.put<CommonApiResponse<QuizResponse>>(`/api/quizzes/${quizId}`, quiz);
    },

    // 추천 퀴즈 조회
    getRecommendedQuizzes: (limit = 5) => {
        return api.get<CommonApiResponse<QuizSummaryResponse[]>>('/api/quizzes/recommended', {
            params: { limit },
        });
    },

    // 데일리 퀴즈 조회
    getDailyQuiz: () => {
        return api.get<CommonApiResponse<QuizResponse>>('/api/quizzes/daily');
    },

    // 태그별 퀴즈 조회
    getQuizzesByTag: (tagId: number, page = 0, size = 10) => {
        return api.get<CommonApiResponse<PageResponse<QuizSummaryResponse>>>(`/api/quizzes/tags/${tagId}`, {
            params: {
                page,
                size,
            },
        });
    },

    // 퀴즈 통계 조회
    getQuizStatistics: (quizId: number) => {
        return api.get<CommonApiResponse<any>>(`/api/quizzes/${quizId}/statistics`);
    },

    // 플레이 가능한 퀴즈 조회 (문제 포함)
    getPlayableQuiz: (quizId: number) => {
        return api.get<CommonApiResponse<QuizResponse>>(`/api/quizzes/${quizId}/play`);
    },
};