// src/utils/auth.ts - 인증 유틸리티 함수
export const getAuthHeader = () => {
    const token = localStorage.getItem('auth_token');
    if (!token) return null;

    return {
        Authorization: `Bearer ${token}`
    };
};