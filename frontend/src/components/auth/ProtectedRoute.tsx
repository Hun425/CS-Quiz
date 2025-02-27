// src/components/auth/ProtectedRoute.tsx
import React, { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { saveAuthRedirect } from '../../utils/authUtils';

interface ProtectedRouteProps {
    children: React.ReactNode;
}

/**
 * 인증된 사용자만 접근할 수 있는 라우트를 보호합니다.
 */
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const location = useLocation();
    const { isAuthenticated, isTokenExpired } = useAuthStore();

    // 인증 상태 확인
    const authenticated = isAuthenticated && !isTokenExpired();

    useEffect(() => {
        // 인증이 필요한 페이지 경로 저장
        if (!authenticated) {
            saveAuthRedirect(location.pathname + location.search);
        }
    }, [authenticated, location]);

    // 인증되지 않은 사용자는 로그인 페이지로 리다이렉트
    if (!authenticated) {
        return <Navigate to="/login" replace />;
    }

    // 인증된 사용자는 원래 페이지로 접근 허용
    return <>{children}</>;
};

export default ProtectedRoute;