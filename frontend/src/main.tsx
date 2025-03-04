// src/main.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import Routes from './routes';
import {AuthProvider} from './providers/AuthProvider';
import './index.css';

if (typeof global === 'undefined') {
    window.global = window;
}

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <AuthProvider>
            <Routes />
        </AuthProvider>
    </React.StrictMode>


);

