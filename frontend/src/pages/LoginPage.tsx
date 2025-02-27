// src/pages/LoginPage.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';

const LoginPage: React.FC = () => {
    const navigate = useNavigate();

    const handleOAuthLogin = (provider: string) => {
        // OAuth2 인증 URL로 리다이렉트
        window.location.href = `http://localhost:8080/api/oauth2/authorize/${provider}`;
    };

    return (
        <div style={{
            maxWidth: '400px',
            margin: '0 auto',
            padding: '2rem',
            borderRadius: '8px',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
            backgroundColor: '#fff',
        }}>
            <h2 style={{ textAlign: 'center', marginBottom: '2rem' }}>로그인</h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <button
                    onClick={() => handleOAuthLogin('google')}
                    style={{
                        padding: '0.75rem',
                        borderRadius: '4px',
                        border: 'none',
                        backgroundColor: '#4285F4',
                        color: 'white',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '0.5rem',
                    }}
                >
                    Google로 로그인
                </button>

                <button
                    onClick={() => handleOAuthLogin('github')}
                    style={{
                        padding: '0.75rem',
                        borderRadius: '4px',
                        border: 'none',
                        backgroundColor: '#333',
                        color: 'white',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '0.5rem',
                    }}
                >
                    GitHub로 로그인
                </button>

                <button
                    onClick={() => handleOAuthLogin('kakao')}
                    style={{
                        padding: '0.75rem',
                        borderRadius: '4px',
                        border: 'none',
                        backgroundColor: '#FEE500',
                        color: '#000',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '0.5rem',
                    }}
                >
                    Kakao로 로그인
                </button>
            </div>

            <p style={{ textAlign: 'center', marginTop: '1rem', fontSize: '0.9rem' }}>
                로그인하여 퀴즈를 풀고 점수를 기록하세요!
            </p>
        </div>
    );
};

export default LoginPage;