// src/providers/AuthProvider.tsx (또는 src/context/AuthProvider.tsx)
import React, { createContext, useContext, useEffect, useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { refreshAccessToken } from '../utils/authUtils';

interface AuthContextType {
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};



export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [loading, setLoading] = useState(true);
    const { isAuthenticated } = useAuthStore();

    // isTokenExpired를 직접 가져오는 대신 함수로 접근
    const isTokenExpired = () => {
        const expiresAt = useAuthStore.getState().expiresAt;
        console.log('만료 시간:', expiresAt, '현재 시간:', Date.now());
        return !expiresAt || Date.now() >= expiresAt;
    };

    const initializeAuth = async () => {
        try {
            if (isAuthenticated && isTokenExpired()) {
                // 토큰이 만료된 경우 refresh 시도
                const refreshed = await refreshAccessToken();
                if (!refreshed) {
                    // refresh 실패 시 로그아웃
                    useAuthStore.getState().logout();
                }
            }
        } catch (error) {
            console.error('인증 초기화 중 오류:', error);
            useAuthStore.getState().logout();
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        initializeAuth();
    }, []);

    return (
        <AuthContext.Provider value={{ loading }}>
            {children}
        </AuthContext.Provider>
    );
};