// src/utils/authUtils.ts
import { useAuthStore } from '../store/authStore';
import { authApi } from '../api/authApi';

/**
 * 사용자가 인증되었는지 확인합니다.
 * 토큰 만료 여부도 검사합니다.
 */
export const isAuthenticated = (): boolean => {
    const store = useAuthStore.getState();
    return store.isAuthenticated && !isTokenExpired();
};

/**
 * 토큰이 만료되었는지 확인합니다.
 */
export const isTokenExpired = (): boolean => {
    const { expiresAt } = useAuthStore.getState();
    if (!expiresAt) return true;
    return Date.now() >= expiresAt;
};

/**
 * 액세스 토큰을 갱신합니다.
 * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 요청합니다.
 */
export const refreshAccessToken = async (): Promise<boolean> => {
    try {
        const { refreshToken } = useAuthStore.getState();

        if (!refreshToken) {
            return false;
        }

        const response = await authApi.refreshToken(refreshToken);

        if (response.data.success) {
            const authData = response.data.data;
            const { user } = useAuthStore.getState();

            if (user) {
                useAuthStore.getState().login(
                    authData.accessToken,
                    authData.refreshToken,
                    user,
                    authData.expiresIn
                );
                return true;
            }
        }

        return false;
    } catch (error) {
        console.error('토큰 갱신 실패:', error);
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