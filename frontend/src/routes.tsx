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
import NotFoundPage from './pages/NotFoundPage';

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
                element: <QuizPlayPage />,
            },
            {
                path: 'quizzes/:quizId/results',
                element: <QuizResultsPage />,
            },
            {
                path: 'login',
                element: <LoginPage />,
            },
        ],
    },
]);

const Routes: React.FC = () => {
    return <RouterProvider router={router} />;
};

export default Routes;