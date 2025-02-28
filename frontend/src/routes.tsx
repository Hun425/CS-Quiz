// src/routes.tsx
import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import App from './App';
import HomePage from './pages/HomePage';
import QuizListPage from './pages/QuizListPage';
import QuizDetailPage from './pages/QuizDetailPage';
import QuizPlayPage from './pages/QuizPlayPage';
import QuizResultsPage from './pages/QuizResultsPage';
import LoginPage from './pages/LoginPage';
import OAuth2CallbackPage from './pages/OAuth2CallbackPage';
import NotFoundPage from './pages/NotFoundPage';
import ProtectedRoute from './components/auth/ProtectedRoute';
import ProfilePage from './pages/ProfilePage';
import QuizCreatePage from "@/pages/QuizCreatePage.tsx";

const router = createBrowserRouter([
    {
        path: '/',
        element: <App />,
        errorElement: <NotFoundPage />,
        children: [
            {
                index: true,
                element: <HomePage />,
            },
            {
                path: 'quizzes',
                element: <QuizListPage />,
            },
            {
                path: 'quizzes/:quizId',
                element: <QuizDetailPage />,
            },
            {
                path: 'quizzes/:quizId/play',
                element: (
                    <ProtectedRoute>
                        <QuizPlayPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: 'quizzes/:quizId/results',
                element: (
                    <ProtectedRoute>
                        <QuizResultsPage />
                    </ProtectedRoute>
                ),
            },
            {
                path: 'login',
                element: <LoginPage />,
            },
            {
                path: 'api/oauth2/callback/:provider',
                element: <OAuth2CallbackPage />,
            },
            {
                path: 'profile',
                element: (
                    <ProtectedRoute>
                        <div>프로필 페이지 (구현 예정)</div>
                    </ProtectedRoute>
                ),
            },
            {
                path: 'my-quizzes',
                element: (
                    <ProtectedRoute>
                        <div>내 퀴즈 페이지 (구현 예정)</div>
                    </ProtectedRoute>
                ),
            },

            // 내 프로필 페이지
            {
                path: 'profile',
                element: (
                    <ProtectedRoute>
                        <ProfilePage />
                    </ProtectedRoute>
                ),
            },

            // 다른 사용자 프로필 페이지
            {
                path: 'profile/:userId',
                element: <ProfilePage />,
            },

            // 퀴즈 생성 페이지
            {
                path: 'QuizCreatePage',
                element: (
                    <ProtectedRoute>
                        <QuizCreatePage />
                    </ProtectedRoute>
                ),
            },
        ],
    },
]);

const Routes: React.FC = () => {
    return <RouterProvider router={router} />;
};

export default Routes;