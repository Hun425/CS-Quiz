// src/pages/OAuth2CallbackPage.tsx
import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import axios, { AxiosError } from 'axios';
import { AuthResponse, UserResponse } from '../types/auth';

const OAuth2CallbackPage: React.FC = () => {
    const { provider } = useParams<{ provider: string }>();
    const location = useLocation();
    const navigate = useNavigate();
    const { login } = useAuthStore();

    const [error, setError] = useState<string | null>(null);
    const [processing, setProcessing] = useState<boolean>(true);

    useEffect(() => {
        // 인가 코드 추출
        const searchParams = new URLSearchParams(location.search);
        const code = searchParams.get('code');

        if (!code) {
            setError('인증 코드가 없습니다. 다시 로그인해 주세요.');
            setProcessing(false);
            return;
        }

        const handleOAuth2Callback = async (): Promise<void> => {
            try {
                // 백엔드에 인가 코드 전송하여 토큰 발급 요청
                const response = await axios.get<AuthResponse>(
                    `http://localhost:8080/api/oauth2/callback/${provider}`,
                    { params: { code } }
                );

                // 응답 데이터 확인
                const { accessToken, refreshToken, expiresIn } = response.data;

                if (accessToken) {
                    // 사용자 정보 가져오는 API 호출
                    const userResponse = await axios.get<UserResponse>(
                        'http://localhost:8080/api/users/me',
                        {
                            headers: {
                                Authorization: `Bearer ${accessToken}`
                            }
                        }
                    );

                    // 로그인 상태 저장
                    login(
                        accessToken,
                        refreshToken,
                        {
                            id: userResponse.data.id,
                            username: userResponse.data.username,
                            email: userResponse.data.email,
                            profileImage: userResponse.data.profileImage,
                            level: userResponse.data.level
                        },
                        expiresIn
                    );

                    // 리다이렉션 처리
                    const redirectTo = localStorage.getItem('authRedirect') || '/';
                    localStorage.removeItem('authRedirect'); // 리다이렉트 URL 삭제
                    navigate(redirectTo);
                } else {
                    setError('로그인 중 오류가 발생했습니다. 다시 시도해 주세요.');
                    setProcessing(false);
                }
            } catch (err: unknown) {
                console.error('OAuth 콜백 처리 중 오류:', err);

                if (err instanceof AxiosError) {
                    setError(`인증 처리 중 오류가 발생했습니다: ${err.message || '서버 오류'}`);
                } else if (err instanceof Error) {
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