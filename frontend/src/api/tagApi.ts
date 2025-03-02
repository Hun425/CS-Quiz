// src/api/tagApi.ts
import { api } from './client';
import { CommonApiResponse, PageResponse, TagResponse } from '../types/api';

// 태그 API 함수들
export const tagApi = {
    // 모든 태그 조회
    getAllTags: () => {
        return api.get<CommonApiResponse<TagResponse[]>>('/api/tags');
    },

    // 태그 검색
    searchTags: (name?: string, page = 0, size = 20) => {
        return api.get<CommonApiResponse<PageResponse<TagResponse>>>('/api/tags/search', {
            params: {
                name,
                page,
                size,
            },
        });
    },

    // 태그 상세 조회
    getTag: (tagId: number) => {
        return api.get<CommonApiResponse<TagResponse>>(`/api/tags/${tagId}`);
    },

    // 루트 태그 조회
    getRootTags: () => {
        return api.get<CommonApiResponse<TagResponse[]>>('/api/tags/roots');
    },

    // 인기 태그 조회
    getPopularTags: (limit = 10) => {
        return api.get<CommonApiResponse<TagResponse[]>>('/api/tags/popular', {
            params: { limit },
        });
    },

    // 태그 생성 (관리자 전용)
    createTag: (tag: {
        name: string;
        description?: string;
        parentId?: number;
        synonyms?: string[];
    }) => {
        return api.post<CommonApiResponse<TagResponse>>('/api/tags', tag);
    },

    // 태그 수정 (관리자 전용)
    updateTag: (tagId: number, tag: {
        name: string;
        description?: string;
        parentId?: number;
        synonyms?: string[];
    }) => {
        return api.put<CommonApiResponse<TagResponse>>(`/api/tags/${tagId}`, tag);
    },

    // 태그 삭제 (관리자 전용)
    deleteTag: (tagId: number) => {
        return api.delete<CommonApiResponse<void>>(`/api/tags/${tagId}`);
    },

    // 자식 태그 조회
    getChildTags: (parentId: number) => {
        return api.get<CommonApiResponse<TagResponse[]>>(`/api/tags/${parentId}/children`);
    },
};