// src/components/quiz/QuizCard.tsx
import React from 'react';
import { Link } from 'react-router-dom';
import { QuizSummaryResponse } from '../../types/api';

interface QuizCardProps {
    quiz: QuizSummaryResponse;
}

const QuizCard: React.FC<QuizCardProps> = ({ quiz }) => {
    // 난이도에 따른 색상
    const getDifficultyColor = (level: string) => {
        switch (level) {
            case 'BEGINNER': return '#4caf50';  // 초록색
            case 'INTERMEDIATE': return '#ff9800';  // 주황색
            case 'ADVANCED': return '#f44336';  // 빨간색
            default: return '#9e9e9e';  // 회색
        }
    };

    return (
        <div style={{
            borderRadius: '8px',
            padding: '16px',
            border: '1px solid #e0e0e0',
            boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
            marginBottom: '16px',
            transition: 'transform 0.2s ease',
            cursor: 'pointer',
        }}>
            <Link to={`/quizzes/${quiz.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                <h3 style={{ marginTop: 0 }}>{quiz.title}</h3>

                <div style={{ display: 'flex', marginBottom: '8px' }}>
          <span style={{
              backgroundColor: getDifficultyColor(quiz.difficultyLevel),
              color: 'white',
              padding: '2px 8px',
              borderRadius: '4px',
              fontSize: '0.8rem',
              marginRight: '8px'
          }}>
            {quiz.difficultyLevel}
          </span>

                    <span style={{
                        backgroundColor: '#e0e0e0',
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontSize: '0.8rem'
                    }}>
            {quiz.quizType.replace('_', ' ')}
          </span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                    <span>문제 수: {quiz.questionCount}</span>
                    <span>평균 점수: {quiz.avgScore?.toFixed(1) || '아직 없음'}</span>
                </div>

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                    {quiz.tags.map(tag => (
                        <span key={tag.id} style={{
                            backgroundColor: '#f0f0f0',
                            padding: '2px 8px',
                            borderRadius: '4px',
                            fontSize: '0.8rem'
                        }}>
              {tag.name}
            </span>
                    ))}
                </div>
            </Link>
        </div>
    );
};

export default QuizCard;