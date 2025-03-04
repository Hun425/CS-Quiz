// src/components/auth/ProtectedRoute.tsx
import React, { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { saveAuthRedirect } from '../../utils/authUtils';

// `children` prop을 위한 인터페이스 정의
interface ProtectedRouteProps {
    children: React.ReactNode; // `React.ReactNode`는 JSX 자식을 표현하는 타입
}

// `children`을 props로 받아서 사용
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const location = useLocation();
    const { isAuthenticated, expiresAt } = useAuthStore();
    const isTokenValid = expiresAt ? Date.now() < expiresAt : false;
    const authenticated = isAuthenticated && isTokenValid;

    useEffect(() => {
        if (!authenticated) {
            saveAuthRedirect(location.pathname + location.search);
        }
    }, [authenticated, location]);

    // 인증 여부에 따라 children을 렌더링하거나 로그인 페이지로 리다이렉트
    return authenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;