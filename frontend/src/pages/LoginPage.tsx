// src/pages/LoginPage.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

// 소셜 로그인 제공자 타입
type Provider = 'google' | 'github' | 'kakao';

// 소셜 로그인 버튼 스타일 맵
const providerStyles: Record<Provider, { bg: string; color: string; text: string }> = {
    google: {
        bg: '#ffffff',
        color: '#757575',
        text: 'Google로 로그인'
    },
    github: {
        bg: '#333333',
        color: '#ffffff',
        text: 'GitHub로 로그인'
    },
    kakao: {
        bg: '#FEE500',
        color: '#000000',
        text: 'Kakao로 로그인'
    }
};

const LoginPage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { isAuthenticated } = useAuthStore();
    const [loggingIn, setLoggingIn] = useState<Provider | null>(null);

    // 이미 로그인된 경우 리다이렉트
    useEffect(() => {
        if (isAuthenticated) {
            const from = location.state?.from || '/';
            navigate(from, { replace: true });
        }
    }, [isAuthenticated, navigate, location]);

    const handleOAuth2Login = (provider: Provider) => {
        setLoggingIn(provider);

        // 현재 위치 저장 (리다이렉트 후 돌아올 수 있도록)
        const from = location.state?.from || '/';
        localStorage.setItem('authRedirect', from);

        // OAuth2 인증 URL로 리다이렉트
        const redirectUrl = `http://localhost:8080/api/oauth2/authorize/${provider}`;
        window.location.href = redirectUrl;
    };

    return (
        <div className="login-page">
            <div className="login-container" style={{
                maxWidth: '400px',
                margin: '2rem auto',
                padding: '2rem',
                borderRadius: '8px',
                boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
                backgroundColor: '#fff',
            }}>
                <h1 style={{ textAlign: 'center', marginBottom: '2rem' }}>로그인</h1>

                <div className="login-options" style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '1rem'
                }}>
                    {/* 소셜 로그인 버튼들 */}
                    {(Object.keys(providerStyles) as Provider[]).map((provider) => (
                        <button
                            key={provider}
                            onClick={() => handleOAuth2Login(provider)}
                            disabled={loggingIn !== null}
                            style={{
                                padding: '0.75rem 1rem',
                                borderRadius: '4px',
                                border: provider === 'google' ? '1px solid #dadce0' : 'none',
                                backgroundColor: providerStyles[provider].bg,
                                color: providerStyles[provider].color,
                                cursor: loggingIn === null ? 'pointer' : 'not-allowed',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                gap: '0.75rem',
                                fontSize: '0.9rem',
                                fontWeight: '500',
                                position: 'relative',
                                opacity: loggingIn !== null && loggingIn !== provider ? 0.5 : 1,
                            }}
                        >
                            {/* 로그인 중 스피너 */}
                            {loggingIn === provider && (
                                <span style={{
                                    display: 'inline-block',
                                    width: '16px',
                                    height: '16px',
                                    border: '2px solid rgba(0, 0, 0, 0.1)',
                                    borderTopColor: providerStyles[provider].color,
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite',
                                    marginRight: '0.5rem'
                                }}></span>
                            )}

                            {/* 로고 (여기서는 생략) */}
                            {providerStyles[provider].text}
                        </button>
                    ))}
                </div>

                <div className="login-info" style={{
                    marginTop: '2rem',
                    textAlign: 'center',
                    fontSize: '0.9rem',
                    color: '#666'
                }}>
                    <p>소셜 계정으로 간편하게 로그인하세요.</p>
                    <p>계정이 없으면 자동으로 가입됩니다.</p>
                </div>
            </div>

            {/* 애니메이션 스타일 */}
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

export default LoginPage;