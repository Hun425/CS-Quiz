// src/types/battle.ts
// API에서 사용되는 배틀 관련 타입 정의

// 배틀룸 응답
export interface BattleRoomResponse {
    id: number;
    quizId: number;
    quizTitle: string;
    status: 'WAITING' | 'IN_PROGRESS' | 'FINISHED';
    roomCode: string;
    currentParticipants: number;
    maxParticipants: number;
    questionCount: number;
    timeLimit: number;
    participants: {
        userId: number;
        username: string;
        profileImage?: string;
        level: number;
        isReady: boolean;
    }[];
    createdAt: string;
}

// 배틀룸 생성 요청
export interface BattleRoomCreateRequest {
    quizId: number;
    maxParticipants?: number;
}

// 배틀 참가 요청 (WebSocket)
export interface BattleJoinRequest {
    roomId: number;
    userId: number;
}

// 배틀 참가 응답 (WebSocket)
export interface BattleJoinResponse {
    roomId: number;
    userId: number;
    username: string;
    currentParticipants: number;
    maxParticipants: number;
    participants: {
        userId: number;
        username: string;
        profileImage?: string;
        level: number;
        isReady: boolean;
    }[];
    joinedAt: string;
}

// 배틀 시작 응답 (WebSocket)
export interface BattleStartResponse {
    roomId: number;
    participants: {
        userId: number;
        username: string;
        profileImage?: string;
        level: number;
    }[];
    totalQuestions: number;
    timeLimit: number;
    startTime: string;
    firstQuestion: BattleNextQuestionResponse;
}

// 다음 문제 응답 (WebSocket)
export interface BattleNextQuestionResponse {
    questionId: number;
    questionText: string;
    questionType: string;
    options: string[];
    timeLimit: number;
    points: number;
    isLastQuestion: boolean;
    isGameOver: boolean;
}

// 답변 제출 요청 (WebSocket)
export interface BattleAnswerRequest {
    roomId: number;
    questionId: number;
    answer: string;
    timeSpentSeconds: number;
}

// 답변 결과 응답 (WebSocket)
export interface BattleAnswerResponse {
    questionId: number;
    isCorrect: boolean;
    earnedPoints: number;
    timeBonus: number;
    currentScore: number;
    correctAnswer: string;
    explanation: string;
}

// 배틀 진행 상황 응답 (WebSocket)
export interface BattleProgressResponse {
    roomId: number;
    currentQuestionIndex: number;
    totalQuestions: number;
    remainingTimeSeconds: number;
    participantProgress: {
        [userId: string]: {
            userId: number;
            username: string;
            currentScore: number;
            correctAnswers: number;
            totalAnswered: number;
            hasAnsweredCurrent: boolean;
            currentStreak: number;
        }
    };
    status: 'WAITING' | 'IN_PROGRESS' | 'FINISHED';
}

// 배틀 종료 응답 (WebSocket)
export interface BattleEndResponse {
    roomId: number;
    results: {
        userId: number;
        username: string;
        finalScore: number;
        correctAnswers: number;
        experienceGained: number;
        isWinner: boolean;
        questionResults?: {
            questionId: number;
            isCorrect: boolean;
            earnedPoints: number;
            timeSpentSeconds: number;
        }[];
    }[];
    totalQuestions: number;
    timeTakenSeconds: number;
    endTime: string;
}

// 기존 API에 추가되는 타입들
// src/types/api.ts에 아래 타입들을 추가해야 합니다.
export * from './battle';