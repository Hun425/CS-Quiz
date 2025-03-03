// src/api/quizApi.ts - 퀴즈 관련 API 연동
import axios from 'axios';
import { QuizResponse, QuizDetailResponse, QuizSummaryResponse, PageResponse, QuizCreateRequest, QuizSubmitRequest, QuizResultResponse } from '../types/api';
import { getAuthHeader } from '../utils/auth';

const BASE_URL = 'http://localhost:8080/api';

// Axios 인스턴스 생성
const apiClient = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 요청 인터셉터 추가
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

export const quizApi = {
    // 퀴즈 검색
    searchQuizzes: async (searchParams: any, page: number = 0, size: number = 10) => {
        const params = new URLSearchParams();
        if (searchParams.title) params.append('title', searchParams.title);
        if (searchParams.difficultyLevel) params.append('difficultyLevel', searchParams.difficultyLevel);
        if (searchParams.quizType) params.append('quizType', searchParams.quizType);
        if (searchParams.tagIds && searchParams.tagIds.length > 0) {
            searchParams.tagIds.forEach((tagId: number) => params.append('tagIds', tagId.toString()));
        }

        params.append('page', page.toString());
        params.append('size', size.toString());

        return apiClient.get<{ success: boolean, data: PageResponse<QuizSummaryResponse> }>(`/quizzes/search?${params.toString()}`);
    },

    // 퀴즈 상세 정보 조회
    getQuiz: async (quizId: number) => {
        return apiClient.get<{ success: boolean, data: QuizDetailResponse }>(`/quizzes/${quizId}`);
    },

    // 플레이 가능한 퀴즈 조회
    getPlayableQuiz: async (quizId: number) => {
        return apiClient.get<{ success: boolean, data: QuizResponse }>(`/quizzes/${quizId}/play`);
    },

    // 퀴즈 시작 (퀴즈 시도 생성)
    startQuiz: async (quizId: number) => {
        return apiClient.post<{ success: boolean, data: { attemptId: number } }>(`/quizzes/${quizId}/start`);
    },

    // 퀴즈 답변 제출
    submitQuiz: async (quizId: number, submitData: QuizSubmitRequest) => {
        return apiClient.post<{ success: boolean, data: QuizResultResponse }>(`/quizzes/${quizId}/results`, submitData);
    },

    // 퀴즈 결과 조회
    getQuizResult: async (quizId: number, attemptId: number) => {
        return apiClient.get<{ success: boolean, data: QuizResultResponse }>(`/quizzes/${quizId}/results/${attemptId}`);
    },

    // 데일리 퀴즈 조회
    getDailyQuiz: async () => {
        return apiClient.get<{ success: boolean, data: QuizResponse }>('/quizzes/daily');
    },

    // 추천 퀴즈 조회
    getRecommendedQuizzes: async (limit: number = 5) => {
        return apiClient.get<{ success: boolean, data: QuizSummaryResponse[] }>(`/quizzes/recommended?limit=${limit}`);
    },

    // 새 퀴즈 생성
    createQuiz: async (quizData: QuizCreateRequest) => {
        return apiClient.post<{ success: boolean, data: QuizResponse }>('/quizzes', quizData);
    },

    // 태그별 퀴즈 조회
    getQuizzesByTag: async (tagId: number, page: number = 0, size: number = 10) => {
        return apiClient.get<{ success: boolean, data: PageResponse<QuizSummaryResponse> }>(`/quizzes/tags/${tagId}?page=${page}&size=${size}`);
    },

    // 퀴즈 통계 조회
    getQuizStatistics: async (quizId: number) => {
        return apiClient.get<{ success: boolean, data: any }>(`/quizzes/${quizId}/statistics`);
    }
};