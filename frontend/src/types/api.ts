// src/types/api.ts
export interface CommonApiResponse<T> {
    success: boolean;
    data: T;
    message?: string;
    timestamp: string;
    code: string;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

// 인증 응답 타입
export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    email: string;
    username: string;
    tokenType: string;
    expiresIn: number;
}

// 태그 관련 타입
export interface TagResponse {
    id: number;
    name: string;
    description: string;
    quizCount: number;
    synonyms: string[];
}

export interface TagCreateRequest {
    name: string;
    description?: string;
    parentId?: number;
    synonyms?: string[];
}

// 퀴즈 관련 타입
export interface QuestionResponse {
    id: number;
    questionType: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'CODE_ANALYSIS' | 'DIAGRAM_BASED';
    questionText: string;
    codeSnippet?: string;
    diagramData?: string;
    options: string[];
    explanation: string;
    points: number;
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    timeLimitSeconds: number;
}

export interface QuestionCreateRequest {
    questionType: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'CODE_ANALYSIS' | 'DIAGRAM_BASED';
    questionText: string;
    codeSnippet?: string;
    diagramData?: string;
    options?: string[];
    correctAnswer: string;
    explanation: string;
    points: number;
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
}

export interface QuizDetailResponse {
    id: number;
    title: string;
    description: string;
    quizType: 'DAILY' | 'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM';
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    timeLimit: number;
    questionCount: number;
    tags: TagResponse[];
    creator: {
        id: number;
        username: string;
        profileImage: string | null;
        level: number;
        joinedAt: string;
    };
    statistics?: {
        totalAttempts: number;
        averageScore: number;
        completionRate: number;
        averageTimeSeconds: number;
        difficultyDistribution: Record<string, number>;
        questionStatistics?: {
            questionId: number;
            correctAnswers: number;
            totalAttempts: number;
            correctRate: number;
            averageTimeSeconds: number;
        }[];
    };
    createdAt: string;
}

export interface QuizResponse extends QuizDetailResponse {
    questions: QuestionResponse[];
    quizAttemptId?: number;
}

export interface QuizSummaryResponse {
    id: number;
    title: string;
    quizType: 'DAILY' | 'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM';
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    questionCount: number;
    attemptCount: number;
    avgScore: number;
    tags: TagResponse[];
    createdAt: string;
}

export interface QuizCreateRequest {
    title: string;
    description: string;
    quizType: 'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM';
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    timeLimit: number;
    tagIds: number[];
    questions: QuestionCreateRequest[];
}

export interface QuizSearchRequest {
    title?: string;
    difficultyLevel?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    quizType?: 'DAILY' | 'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM';
    tagIds?: number[];
    minQuestions?: number;
    maxQuestions?: number;
    orderBy?: string;
}

export interface QuizSubmitRequest {
    quizAttemptId: number;
    answers: Record<number, string>;
    timeTaken?: number;
}

export interface QuizResultResponse {
    quizId: number;
    title: string;
    totalQuestions: number;
    correctAnswers: number;
    score: number;
    totalPossibleScore: number;
    timeTaken: number;
    completedAt: string;
    experienceGained: number;
    newTotalExperience: number;
    questions: {
        id: number;
        questionText: string;
        yourAnswer: string;
        correctAnswer: string;
        isCorrect: boolean;
        explanation: string;
        points: number;
    }[];
}

// 배틀 관련 타입
export interface BattleRoomCreateRequest {
    quizId: number;
    maxParticipants?: number;
}

export interface BattleRoomResponse {
    id: number;
    roomCode: string;
    quizTitle: string;
    quizId: number;
    status: string;
    maxParticipants: number;
    currentParticipants: number;
    participants: {
        id: number;
        userId: number;
        username: string;
        profileImage: string | null;
        ready: boolean;
        level: number;
        currentScore: number;
    }[];
    createdAt: string;
    startTime?: string;
    endTime?: string;
    timeLimit?: number;
    questionCount: number;
}

// WebSocket 메시지 응답 타입
export interface BattleJoinResponse {
    participants: {
        userId: number;
        username: string;
        profileImage: string | null;
        level: number;
        isReady: boolean;
    }[];
}

export interface BattleStartResponse {
    firstQuestion: BattleNextQuestionResponse;
}

export interface BattleProgressResponse {
    participantProgress: {
        [userId: number]: {
            userId: number;
            username: string;
            currentScore: number;
            correctAnswers: number;
            totalAnswered: number;
            hasAnsweredCurrent: boolean;
            currentStreak: number;
        };
    };
    currentQuestionIndex: number;
}

export interface BattleNextQuestionResponse {
    questionId: number;
    questionText: string;
    questionType: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'CODE_ANALYSIS' | 'DIAGRAM_BASED';
    options?: string[];
    timeLimit: number;
    points: number;
    isGameOver: boolean;
}

export interface BattleEndResponse {
    results: {
        userId: number;
        username: string;
        finalScore: number;
        correctAnswers: number;
        totalQuestions: number;
        experienceGained: number;
        isWinner: boolean;
    }[];
    totalQuestions: number;
    timeTakenSeconds: number;
    endTime: string;
}

export interface BattleAnswerResponse {
    questionId: number;
    isCorrect: boolean;
    correctAnswer: string;
    explanation: string;
    pointsEarned: number;
}