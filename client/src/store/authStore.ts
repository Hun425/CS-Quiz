// src/store/authStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/authApi';

interface User {
    id: number;
    username: string;
    email: string;
    profileImage?: string;
    level: number;
}

interface AuthState {
    isAuthenticated: boolean;
    user: User | null;
    token: string | null;
    refreshToken: string | null;
    expiresAt: number | null;
    login: (token: string, refreshToken: string, user: User, expiresIn: number) => void;
    logout: () => void;
    updateUser: (userData: Partial<User>) => void;
    refreshAuth: () => Promise<boolean>;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set, get) => ({
            isAuthenticated: false,
            user: null,
            token: null,
            refreshToken: null,
            expiresAt: null,

            login: (token, refreshToken, user, expiresIn) => {
                // Calculate expiration time in milliseconds
                const expiresAt = Date.now() + expiresIn;

                // Save token to localStorage (in addition to the persist middleware)
                localStorage.setItem('auth_token', token);
                localStorage.setItem('refresh_token', refreshToken);

                set({
                    isAuthenticated: true,
                    user,
                    token,
                    refreshToken,
                    expiresAt
                });
            },

            logout: async () => {
                try {
                    // Call logout API
                    await authApi.logout();
                } catch (error) {
                    console.error('Logout error:', error);
                } finally {
                    // Clear local storage
                    localStorage.removeItem('auth_token');
                    localStorage.removeItem('refresh_token');

                    // Reset state
                    set({
                        isAuthenticated: false,
                        user: null,
                        token: null,
                        refreshToken: null,
                        expiresAt: null
                    });
                }
            },

            updateUser: (userData) => {
                const currentUser = get().user;
                if (!currentUser) return;

                set({
                    user: { ...currentUser, ...userData }
                });
            },

            refreshAuth: async () => {
                const { refreshToken, expiresAt } = get();

                // Check if token is about to expire (within 5 minutes)
                const isExpiringSoon = expiresAt && (expiresAt - Date.now()) < 5 * 60 * 1000;

                if (refreshToken && isExpiringSoon) {
                    try {
                        const response = await authApi.refreshToken(refreshToken);

                        if (response.data.success) {
                            const authData = response.data.data;
                            const user = get().user;

                            if (user) {
                                get().login(
                                    authData.accessToken,
                                    authData.refreshToken,
                                    user,
                                    authData.expiresIn
                                );
                                return true;
                            }
                        }
                    } catch (error) {
                        console.error('Token refresh failed:', error);
                        // If refresh fails, force logout
                        get().logout();
                        return false;
                    }
                }

                // Return true if token is still valid
                return !!expiresAt && (expiresAt > Date.now());
            }
        }),
        {
            name: 'auth-storage',
            partialize: (state) => ({
                isAuthenticated: state.isAuthenticated,
                user: state.user,
                token: state.token,
                refreshToken: state.refreshToken,
                expiresAt: state.expiresAt
            })
        }
    )
);

// Setup token refresh check on app initialization
export const initAuthRefresh = () => {
    const checkAndRefreshToken = async () => {
        const { refreshAuth, isAuthenticated } = useAuthStore.getState();

        if (isAuthenticated) {
            await refreshAuth();
        }
    };

    // Check on init
    checkAndRefreshToken();

    // Setup interval to check token (every 4 minutes)
    setInterval(checkAndRefreshToken, 4 * 60 * 1000);
};