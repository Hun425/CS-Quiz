// src/store/authStore.ts
import { create } from 'zustand';

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
    accessToken: string | null;
    refreshToken: string | null;
    login: (accessToken: string, refreshToken: string, user: User) => void;
    logout: () => void;
    updateUser: (user: Partial<User>) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    isAuthenticated: !!localStorage.getItem('accessToken'),
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    accessToken: localStorage.getItem('accessToken'),
    refreshToken: localStorage.getItem('refreshToken'),

    login: (accessToken, refreshToken, user) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('user', JSON.stringify(user));

        set({
            isAuthenticated: true,
            accessToken,
            refreshToken,
            user,
        });
    },

    logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');

        set({
            isAuthenticated: false,
            accessToken: null,
            refreshToken: null,
            user: null,
        });
    },

    updateUser: (userData) => {
        set((state) => {
            const updatedUser = { ...state.user, ...userData } as User;
            localStorage.setItem('user', JSON.stringify(updatedUser));
            return { user: updatedUser };
        });
    },
}));