// src/types/api.ts
// 공통 API 응답 타입
export interface CommonApiResponse<T> {
    success: boolean;
    data: T;
    message: string;
    timestamp: string;
    code: string;
}

// 페이지네이션 응답 타입
export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
}

// 유저 요약 타입
export interface UserSummaryResponse {
    id: number;
    username: string;
    profileImage: string;
    level: number;
    joinedAt: string;
}

// 태그 타입
export interface TagResponse {
    id: number;
    name: string;
    description: string;
    quizCount: number;
    synonyms: string[];
}

// 퀴즈 요약 타입
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

// 질문 타입
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


// 질문 생성 요청 타입
export interface QuestionCreateRequest {
    questionType: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'CODE_ANALYSIS' | 'DIAGRAM_BASED';
    questionText: string;
    codeSnippet?: string;
    diagramData?: string;
    options: string[];
    correctAnswer: string;
    explanation: string;
    points: number;
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
}

// 퀴즈 생성 요청 타입
export interface QuizCreateRequest {
    title: string;
    description: string;
    quizType: 'DAILY' | 'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM';
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    timeLimit: number;
    tagIds: number[];
    questions: QuestionCreateRequest[];
}

// 이 부분이 중요합니다 - QuizDetailResponse와 QuizResponse의 차이점
export interface QuizDetailResponse {
    id: number;
    title: string;
    description: string;
    quizType: 'DAILY' | 'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM';
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    timeLimit: number;
    questionCount: number;
    tags: TagResponse[];
    creator: UserSummaryResponse;
    createdAt: string;
    // questions 속성이 없음
}

export interface QuizResponse {
    id: number;
    title: string;
    description: string;
    quizType: 'DAILY' | 'TAG_BASED' | 'TOPIC_BASED' | 'CUSTOM';
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    timeLimit: number;
    questionCount: number;
    tags: TagResponse[];
    questions: QuestionResponse[]; // 이 속성이 QuizDetailResponse에는 없음
    creator: UserSummaryResponse;
    createdAt: string;
}