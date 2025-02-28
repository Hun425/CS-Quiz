// src/store/authStore.ts
import { create } from 'zustand';
import { persist, PersistOptions } from 'zustand/middleware';
import { User, AuthState } from '../types/auth';

// 지속성 설정 타입 정의
type AuthPersist = Pick<AuthState, 'isAuthenticated' | 'user' | 'accessToken' | 'refreshToken' | 'expiresAt'>;
const persistOptions: PersistOptions<AuthState, AuthPersist> = {
    name: 'auth-storage',
    partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        expiresAt: state.expiresAt
    })
};

// 1시간을 기본 만료 시간으로 설정 (단위: 밀리초)
const DEFAULT_EXPIRATION = 60 * 60 * 1000;

export const useAuthStore = create<AuthState>()(
    persist(
        (set, get) => ({
            isAuthenticated: false,
            user: null,
            accessToken: null,
            refreshToken: null,
            expiresAt: null,

            login: (accessToken: string, refreshToken: string, user: User, expiresIn: number = DEFAULT_EXPIRATION): void => {
                const expiresAt = Date.now() + expiresIn;

                set({
                    isAuthenticated: true,
                    accessToken,
                    refreshToken,
                    user,
                    expiresAt
                });
            },

            logout: (): void => {
                set({
                    isAuthenticated: false,
                    accessToken: null,
                    refreshToken: null,
                    user: null,
                    expiresAt: null
                });
            },

            updateUser: (userData: Partial<User>): void => {
                set((state) => ({
                    user: state.user ? { ...state.user, ...userData } : null
                }));
            },

            updateTokens: (accessToken: string, refreshToken: string, expiresIn: number = DEFAULT_EXPIRATION): void => {
                const expiresAt = Date.now() + expiresIn;

                set({
                    accessToken,
                    refreshToken,
                    expiresAt
                });
            },

            isTokenExpired: (): boolean => {
                const { expiresAt } = get();
                if (!expiresAt) return true;

                // 만료 10분 전부터는 만료된 것으로 간주
                return Date.now() > expiresAt - 10 * 60 * 1000;
            }
        }),
        persistOptions
    )
);