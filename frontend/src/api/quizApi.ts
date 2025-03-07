// src/api/quizApi.ts - 퀴즈 관련 API 연동
import { api } from './client';
import {
    QuizResponse,
    QuizDetailResponse,
    QuizSummaryResponse,
    PageResponse,
    QuizCreateRequest,
    QuizSubmitRequest,
    QuizResultResponse,
    CommonApiResponse
} from '../types/api';

export const quizApi = {
    // 퀴즈 검색
    searchQuizzes: async (searchParams: any, page: number = 0, size: number = 10) => {
        const params = new URLSearchParams();

        // 검색 파라미터 추가
        if (searchParams.title) params.append('title', searchParams.title);
        if (searchParams.difficultyLevel) params.append('difficultyLevel', searchParams.difficultyLevel);
        if (searchParams.quizType) params.append('quizType', searchParams.quizType);

        // 태그 처리 방식 변경
        if (searchParams.tagIds && searchParams.tagIds.length > 0) {
            // 방법 1: 쉼표로 구분된 문자열로 변환
            params.append('tagIds', searchParams.tagIds.join(','));

            // 또는 방법 2: 여러 개의 tagIds 파라미터 사용 (현재 사용 중인 방식)
            // searchParams.tagIds.forEach((tagId: number) => {
            //     params.append('tagIds', tagId.toString());
            // });
        }

        // 페이지네이션 파라미터
        params.append('page', page.toString());
        params.append('size', size.toString());

        // 디버깅용 로그 추가
        console.log('검색 파라미터:', Object.fromEntries(params.entries()));

        return api.get<CommonApiResponse<PageResponse<QuizSummaryResponse>>>(`/quizzes/search?${params.toString()}`);
    },

    // 퀴즈 상세 정보 조회
    getQuiz: async (quizId: number) => {
        return api.get<CommonApiResponse<QuizDetailResponse>>(`/quizzes/${quizId}`);
    },

    // 플레이 가능한 퀴즈 조회
    getPlayableQuiz: async (quizId: number) => {
        return api.get<CommonApiResponse<QuizResponse>>(`/quizzes/${quizId}/play`);
    },

    // 퀴즈 답변 제출
    submitQuiz: async (quizId: number, submitData: QuizSubmitRequest) => {
        return api.post<CommonApiResponse<QuizResultResponse>>(`/quizzes/${quizId}/results`, submitData);
    },

    // 퀴즈 결과 조회
    getQuizResult: async (quizId: number, attemptId: number) => {
        return api.get<CommonApiResponse<QuizResultResponse>>(`/quizzes/${quizId}/results/${attemptId}`);
    },

    // 데일리 퀴즈 조회
    getDailyQuiz: async () => {
        return api.get<CommonApiResponse<QuizResponse>>('/quizzes/daily');
    },

    // 추천 퀴즈 조회
    getRecommendedQuizzes: async (limit: number = 5) => {
        return api.get<CommonApiResponse<QuizSummaryResponse[]>>(`/quizzes/recommended?limit=${limit}`);
    },

    // 새 퀴즈 생성
    createQuiz: async (quizData: QuizCreateRequest) => {
        return api.post<CommonApiResponse<QuizResponse>>('/quizzes', quizData);
    },

    // 퀴즈 수정
    updateQuiz: async (quizId: number, quizData: QuizCreateRequest) => {
        return api.put<CommonApiResponse<QuizResponse>>(`/quizzes/${quizId}`, quizData);
    },

    // 태그별 퀴즈 조회
    getQuizzesByTag: async (tagId: number, page: number = 0, size: number = 10) => {
        return api.get<CommonApiResponse<PageResponse<QuizSummaryResponse>>>(`/quizzes/tags/${tagId}?page=${page}&size=${size}`);
    },

    // 퀴즈 통계 조회
    getQuizStatistics: async (quizId: number) => {
        return api.get<CommonApiResponse<any>>(`/quizzes/${quizId}/statistics`);
    }
};