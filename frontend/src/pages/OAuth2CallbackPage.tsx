// src/pages/OAuth2CallbackPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

const OAuth2CallbackPage: React.FC = () => {
    const { provider } = useParams<{ provider: string }>();
    const location = useLocation();
    const navigate = useNavigate();
    const { login } = useAuthStore();

    const [error, setError] = useState<string | null>(null);
    const [processing, setProcessing] = useState<boolean>(true);

    useEffect(() => {
        const handleOAuth2Callback = async (): Promise<void> => {
            try {
                // URL에서 token과 refreshToken 파라미터 추출
                const searchParams = new URLSearchParams(location.search);
                const token = searchParams.get('token');
                const refreshToken = searchParams.get('refreshToken');

                if (!token || !refreshToken) {
                    setError('인증 토큰을 받지 못했습니다. 다시 로그인해 주세요.');
                    setProcessing(false);
                    return;
                }

                // 사용자 정보 가져오는 API 호출
                const response = await fetch('http://localhost:8080/api/users/me/profile', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) {
                    throw new Error('사용자 정보를 가져오는데 실패했습니다.');
                }

                const userData = await response.json();

                if (userData.success && userData.data) {
                    const user = userData.data;

                    // 로그인 상태 저장
                    login(
                        token,
                        refreshToken,
                        {
                            id: user.id,
                            username: user.username,
                            email: user.email,
                            profileImage: user.profileImage,
                            level: user.level
                        },
                        3600 * 1000 // 1시간 기본 만료 시간
                    );

                    // 리다이렉션 처리
                    const redirectTo = localStorage.getItem('authRedirect') || '/';
                    localStorage.removeItem('authRedirect'); // 리다이렉트 URL 삭제
                    navigate(redirectTo);
                } else {
                    setError('사용자 정보를 가져오는데 실패했습니다.');
                    setProcessing(false);
                }
            } catch (err: unknown) {
                console.error('OAuth 콜백 처리 중 오류:', err);

                if (err instanceof Error) {
                    setError(`인증 처리 중 오류가 발생했습니다: ${err.message}`);
                } else {
                    setError('인증 처리 중 알 수 없는 오류가 발생했습니다.');
                }

                setProcessing(false);
            }
        };

        handleOAuth2Callback();
    }, [provider, location.search, login, navigate]);

    if (error) {
        return (
            <div className="oauth-callback-error" style={{
                maxWidth: '400px',
                margin: '2rem auto',
                padding: '2rem',
                textAlign: 'center',
                backgroundColor: '#ffebee',
                borderRadius: '8px'
            }}>
                <h2 style={{ color: '#d32f2f', marginTop: 0 }}>로그인 오류</h2>
                <p>{error}</p>
                <button
                    onClick={() => navigate('/login')}
                    style={{
                        backgroundColor: '#1976d2',
                        color: 'white',
                        border: 'none',
                        padding: '0.5rem 1rem',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        marginTop: '1rem'
                    }}
                >
                    로그인 페이지로 돌아가기
                </button>
            </div>
        );
    }

    return (
        <div className="oauth-callback-processing" style={{
            maxWidth: '400px',
            margin: '2rem auto',
            padding: '2rem',
            textAlign: 'center'
        }}>
            <h2>로그인 처리 중...</h2>
            <p>잠시만 기다려 주세요.</p>
            <div className="spinner" style={{
                display: 'inline-block',
                width: '40px',
                height: '40px',
                margin: '1rem 0',
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
};

export default OAuth2CallbackPage;