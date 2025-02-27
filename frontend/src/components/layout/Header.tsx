// src/components/layout/Header.tsx
import React from 'react';

interface HeaderProps {
    title: string;
    subtitle?: string;
}

const Header: React.FC<HeaderProps> = ({ title, subtitle }) => {
    return (
        <header style={{
            padding: '1rem',
            backgroundColor: '#f5f5f5',
            borderBottom: '1px solid #e0e0e0',
            marginBottom: '2rem'
        }}>
            <h1 style={{ margin: 0, color: '#333' }}>{title}</h1>
            {subtitle && <p style={{ margin: '0.5rem 0 0', color: '#666' }}>{subtitle}</p>}
        </header>
    );
};

export default Header;