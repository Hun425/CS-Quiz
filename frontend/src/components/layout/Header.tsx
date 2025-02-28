// src/components/layout/Header.tsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

interface HeaderProps {
    title: string;
    subtitle?: string;
}

const Header: React.FC<HeaderProps> = ({ title, subtitle }) => {
    const navigate = useNavigate();
    const { isAuthenticated, user, logout } = useAuthStore();
    const [showDropdown, setShowDropdown] = useState(false);

    const handleLogout = () => {
        logout();
        navigate('/');
        setShowDropdown(false);
    };

    const toggleDropdown = () => {
        setShowDropdown(!showDropdown);
    };

    return (
        <header style={{
            padding: '1rem',
            backgroundColor: '#1976d2',
            color: 'white',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            marginBottom: '2rem'
        }}>
            <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                maxWidth: '1200px',
                margin: '0 auto'
            }}>
                {/* 로고 및 제목 */}
                <div>
                    <Link to="/" style={{ textDecoration: 'none', color: 'white' }}>
                        <h1 style={{ margin: 0, fontSize: '1.5rem' }}>{title}</h1>
                        {subtitle && <p style={{ margin: '0.5rem 0 0', fontSize: '0.9rem', opacity: 0.8 }}>{subtitle}</p>}
                    </Link>
                </div>

                {/* 네비게이션 */}
                <nav>
                    <ul style={{
                        display: 'flex',
                        gap: '1.5rem',
                        listStyle: 'none',
                        margin: 0,
                        padding: 0
                    }}>
                        <li>
                            <Link to="/" style={{ color: 'white', textDecoration: 'none' }}>
                                홈
                            </Link>
                        </li>
                        <li>
                            <Link to="/quizzes" style={{ color: 'white', textDecoration: 'none' }}>
                                퀴즈 목록
                            </Link>
                        </li>
                        {/* 추가 메뉴 항목 */}
                    </ul>
                </nav>

                {/* 사용자 정보 또는 로그인 버튼 */}
                <div>
                    {isAuthenticated && user ? (
                        <div style={{ position: 'relative' }}>
                            <button
                                onClick={toggleDropdown}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '0.5rem',
                                    background: 'none',
                                    border: 'none',
                                    color: 'white',
                                    cursor: 'pointer',
                                    padding: '0.5rem',
                                    borderRadius: '4px'
                                }}
                            >
                                {user.profileImage ? (
                                    <img
                                        src={user.profileImage}
                                        alt={user.username}
                                        style={{
                                            width: '32px',
                                            height: '32px',
                                            borderRadius: '50%',
                                            objectFit: 'cover'
                                        }}
                                    />
                                ) : (
                                    <div style={{
                                        width: '32px',
                                        height: '32px',
                                        borderRadius: '50%',
                                        backgroundColor: 'rgba(255, 255, 255, 0.2)',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        fontWeight: 'bold'
                                    }}>
                                        {user.username.charAt(0).toUpperCase()}
                                    </div>
                                )}
                                <span>{user.username}</span>
                                <span style={{ fontSize: '0.8rem' }}>▼</span>
                            </button>

                            {/* 드롭다운 메뉴 */}
                            {showDropdown && (
                                <div style={{
                                    position: 'absolute',
                                    top: '100%',
                                    right: 0,
                                    backgroundColor: 'white',
                                    borderRadius: '4px',
                                    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
                                    width: '200px',
                                    zIndex: 10
                                }}>
                                    <ul style={{
                                        listStyle: 'none',
                                        margin: 0,
                                        padding: 0
                                    }}>
                                        <li>
                                            <Link
                                                to="/profile"
                                                style={{
                                                    display: 'block',
                                                    padding: '0.75rem 1rem',
                                                    color: '#333',
                                                    textDecoration: 'none',
                                                    borderBottom: '1px solid #f0f0f0'
                                                }}
                                                onClick={() => setShowDropdown(false)}
                                            >
                                                내 프로필
                                            </Link>
                                        </li>
                                        <li>
                                            <Link
                                                to="/my-quizzes"
                                                style={{
                                                    display: 'block',
                                                    padding: '0.75rem 1rem',
                                                    color: '#333',
                                                    textDecoration: 'none',
                                                    borderBottom: '1px solid #f0f0f0'
                                                }}
                                                onClick={() => setShowDropdown(false)}
                                            >
                                                내 퀴즈
                                            </Link>
                                        </li>
                                        <li>
                                            <button
                                                onClick={handleLogout}
                                                style={{
                                                    display: 'block',
                                                    width: '100%',
                                                    padding: '0.75rem 1rem',
                                                    textAlign: 'left',
                                                    backgroundColor: 'transparent',
                                                    border: 'none',
                                                    color: '#d32f2f',
                                                    cursor: 'pointer'
                                                }}
                                            >
                                                로그아웃
                                            </button>
                                        </li>
                                    </ul>
                                </div>
                            )}
                        </div>
                    ) : (
                        <Link
                            to="/login"
                            style={{
                                display: 'inline-block',
                                padding: '0.5rem 1rem',
                                backgroundColor: 'rgba(255, 255, 255, 0.1)',
                                color: 'white',
                                borderRadius: '4px',
                                textDecoration: 'none',
                                fontWeight: '500'
                            }}
                        >
                            로그인
                        </Link>
                    )}
                </div>
            </div>
        </header>
    );
};

export default Header;