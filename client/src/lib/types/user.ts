// src/types/user.ts
export interface UserProfile {
    id: number;
    username: string;
    email: string;
    profileImage: string | null;
    level: number;
    experience: number;
    requiredExperience: number;
    totalPoints: number;
    joinedAt: string;
    lastLogin: string;
}

export interface UserStatistics {
    totalQuizzesTaken: number;
    totalQuizzesCompleted: number;
    averageScore: number;
    totalCorrectAnswers: number;
    totalQuestions: number;
    correctRate: number;
    totalTimeTaken: number; // 초 단위
    bestScore: number;
    worstScore: number;
}

export interface RecentActivity {
    id: number;
    type: 'QUIZ_ATTEMPT' | 'ACHIEVEMENT_EARNED' | 'LEVEL_UP';
    quizId?: number;
    quizTitle?: string;
    score?: number;
    achievementId?: number;
    achievementName?: string;
    newLevel?: number;
    timestamp: string;
}

export interface Achievement {
    id: number;
    name: string;
    description: string;
    iconUrl: string;
    earnedAt: string | null;
    progress: number; // 달성 진행도 (0-100%)
    requirementDescription: string;
}

export interface TopicPerformance {
    tagId: number;
    tagName: string;
    quizzesTaken: number;
    averageScore: number;
    correctRate: number;
    strength: boolean; // 강점 여부
}