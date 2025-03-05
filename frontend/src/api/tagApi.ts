// src/api/tagApi.ts - 태그 관련 API 연동
import axios, {AxiosHeaders} from 'axios';
import { TagResponse, PageResponse } from '../types/api';
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
            config.headers = new AxiosHeaders({
                ...config.headers,
                ...authHeader,
            });
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const tagApi = {
    // 모든 태그 조회
    getAllTags: async () => {
        return apiClient.get<{ success: boolean, data: TagResponse[] }>('/tags');
    },

    // 태그 검색
    searchTags: async (name?: string, page: number = 0, size: number = 20) => {
        const params = new URLSearchParams();
        if (name) params.append('name', name);
        params.append('page', page.toString());
        params.append('size', size.toString());

        return apiClient.get<{ success: boolean, data: PageResponse<TagResponse> }>(`/tags/search?${params.toString()}`);
    },

    // 태그 상세 조회
    getTag: async (tagId: number) => {
        return apiClient.get<{ success: boolean, data: TagResponse }>(`/tags/${tagId}`);
    },

    // 루트 태그 조회
    getRootTags: async () => {
        return apiClient.get<{ success: boolean, data: TagResponse[] }>('/tags/roots');
    },

    // 인기 태그 조회
    getPopularTags: async (limit: number = 10) => {
        return apiClient.get<{ success: boolean, data: TagResponse[] }>(`/tags/popular?limit=${limit}`);
    },

    // 자식 태그 조회
    getChildTags: async (parentId: number) => {
        return apiClient.get<{ success: boolean, data: TagResponse[] }>(`/tags/${parentId}/children`);
    },

    // 태그 생성 (추가됨)
    createTag: async (tagData: any) => {
        return apiClient.post<{ success: boolean, data: TagResponse }>('/tags', tagData);
    },

    // 태그 수정 (추가됨)
    updateTag: async (tagId: number, tagData: any) => {
        return apiClient.put<{ success: boolean, data: TagResponse }>(`/tags/${tagId}`, tagData);
    },

    // 태그 삭제 (추가됨)
    deleteTag: async (tagId: number) => {
        return apiClient.delete<{ success: boolean, data: any }>(`/tags/${tagId}`);
    }
};