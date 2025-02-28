// src/utils/authUtils.ts
import { useAuthStore } from '../store/authStore';
import { api } from '../api/client';
import { AuthResponse } from '../types/auth';

/**
 * 사용자가 인증되었는지 확인합니다.
 * 토큰 만료 여부도 검사합니다.
 */
export const isAuthenticated = (): boolean => {
    const { isAuthenticated, isTokenExpired } = useAuthStore.getState();
    return isAuthenticated && !isTokenExpired();
};

/**
 * 액세스 토큰을 갱신합니다.
 * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 요청합니다.
 */
export const refreshAccessToken = async (): Promise<boolean> => {
    const { refreshToken, updateTokens } = useAuthStore.getState();

    if (!refreshToken) return false;

    try {
        // 백엔드는 refreshToken을 Authorization 헤더로 기대합니다
        const response = await api.post<AuthResponse>(
            '/api/oauth2/refresh',
            null, // 요청 바디가 없음
            {
                headers: {
                    'Authorization': refreshToken // "Bearer" 접두사 없이 전송
                }
            }
        );

        if (response.data.accessToken) {
            const { accessToken, refreshToken: newRefreshToken, expiresIn } = response.data;
            updateTokens(accessToken, newRefreshToken || refreshToken, expiresIn);
            return true;
        }

        return false;
    } catch (error: unknown) {
        console.error('토큰 갱신 중 오류:', error);
        useAuthStore.getState().logout();
        return false;
    }
};

/**
 * 인증이 필요한 URL로 이동하기 전에 현재 URL을 저장합니다.
 */
export const saveAuthRedirect = (path: string): void => {
    localStorage.setItem('authRedirect', path);
};

/**
 * 로그인 상태가 변경될 때 실행할 콜백을 등록합니다.
 */
export const onAuthStateChanged = (callback: (isAuthenticated: boolean) => void): (() => void) => {
    // Zustand subscribe 메소드에 상태 변경 리스너 전달
    const unsubscribe = useAuthStore.subscribe((state, prevState) => {
        // 인증 상태가 변경되었을 때만 콜백 호출
        if (state.isAuthenticated !== prevState.isAuthenticated) {
            callback(state.isAuthenticated);
        }
    });

    return unsubscribe;
};