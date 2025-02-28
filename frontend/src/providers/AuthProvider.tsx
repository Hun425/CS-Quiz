// src/providers/AuthProvider.tsx
import React, { useEffect, useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { refreshAccessToken } from '../utils/authUtils';

interface AuthProviderProps {
    children: React.ReactNode;
}

const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const { isAuthenticated, isTokenExpired } = useAuthStore();
    const [initialized, setInitialized] = useState<boolean>(false);

    // 앱 로드 시 인증 상태 초기화
    useEffect(() => {
        const initializeAuth = async (): Promise<void> => {
            if (isAuthenticated && isTokenExpired()) {
                // 토큰이 만료되었다면 갱신 시도
                await refreshAccessToken();
            }
            setInitialized(true);
        };

        initializeAuth();
    }, [isAuthenticated, isTokenExpired]);

    // 토큰 자동 갱신 처리
    useEffect(() => {
        if (!isAuthenticated) return;

        // 토큰 만료 10분 전에 자동 갱신
        const checkTokenExpiration = async (): Promise<void> => {
            const { expiresAt, isTokenExpired } = useAuthStore.getState();

            if (expiresAt && isTokenExpired()) {
                await refreshAccessToken();
            }
        };

        // 주기적으로 토큰 상태 확인 (1분마다)
        const intervalId = setInterval(checkTokenExpiration, 60 * 1000);

        return () => {
            clearInterval(intervalId);
        };
    }, [isAuthenticated]);

    // 초기화 전에는 로딩 상태 표시
    if (!initialized) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <div style={{
                    display: 'inline-block',
                    width: '40px',
                    height: '40px',
                    border: '4px solid rgba(0, 0, 0, 0.1)',
                    borderTopColor: '#1976d2',
                    borderRadius: '50%',
                    animation: 'spin 1s linear infinite'
                }}></div>
                <style>
                    {`
            @keyframes spin {
              to { transform: rotate(360deg); }
            }
          `}
                </style>
            </div>
        );
    }

    return <>{children}</>;
};

export default AuthProvider;