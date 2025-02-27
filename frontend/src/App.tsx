// src/App.tsx
import React from 'react';
import { Outlet } from 'react-router-dom';
import Header from './components/layout/Header';
import './App.css';

function App() {
    return (
        <div className="App">
            <Header
                title="CS 퀴즈 플랫폼"
                subtitle="컴퓨터 과학 지식을 테스트해보세요!"
            />
            <main style={{ padding: '0 1rem' }}>
                <Outlet />
            </main>
        </div>
    );
}

export default App;