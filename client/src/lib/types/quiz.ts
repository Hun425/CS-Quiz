import { QuestionResponse, QuestionCreateRequest } from "./question";
import { TagResponse } from "./tag";

export enum QuizType {
  DAILY = "DAILY",
  TAG_BASED = "TAG_BASED",
  TOPIC_BASED = "TOPIC_BASED",
  CUSTOM = "CUSTOM",
}

export enum QuizDifficultyType {
  BEGINNER = "BEGINNER",
  INTERMEDIATE = "INTERMEDIATE",
  ADVANCED = "ADVANCED",
}

export interface QuestionStatistics {
  questionId: number;
  correctAnswers: number;
  totalAttempts: number;
  correctRate: number;
  averageTimeSeconds: number;
}

// ✅ 퀴즈 상세 조회 타입
export interface QuizDetailResponse {
  id: number;
  title: string;
  description: string;
  quizType: QuizType;
  difficultyLevel: QuizDifficultyType;
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
    questionStatistics?: QuestionStatistics[];
  };
  createdAt: string;
}

// ✅ 퀴즈 응답 타입 (전체 퀴즈 데이터)
export interface QuizResponse extends QuizDetailResponse {
  questions: QuestionResponse[];
  quizAttemptId?: number;
}

// ✅ 퀴즈 요약 타입 (리스트에서 사용)
export interface QuizSummaryResponse {
  id: number;
  title: string;
  quizType: QuizType;
  difficultyLevel: QuizDifficultyType;
  questionCount: number;
  attemptCount: number;
  avgScore: number;
  tags: TagResponse[];
  createdAt: string;
}

export enum QuizCreateType {
  TAG_BASED = "TAG_BASED",
  TOPIC_BASED = "TOPIC_BASED",
  CUSTOM = "CUSTOM",
}

// ✅ 퀴즈 생성 요청 타입
export interface QuizCreateRequest {
  title: string;
  description: string;
  quizType: QuizCreateType;
  difficultyLevel: QuizDifficultyType;
  timeLimit: number;
  tagIds: number[];
  questions: QuestionCreateRequest[];
}

// ✅ 퀴즈 검색 요청 타입
export interface QuizSearchRequest {
  title?: string;
  difficultyLevel?: QuizCreateType;
  quizType?: QuizDifficultyType;
  tagIds?: number[];
  minQuestions?: number;
  maxQuestions?: number;
  orderBy?: "title" | "difficultyLevel" | "quizType" | "createdAt"; // ✅ 안전한 값만 허용
}

// ✅ 퀴즈 제출 요청 타입
export interface QuizSubmitRequest {
  quizAttemptId: number;
  answers: Record<number, string>; // ✅ 문제 ID별 사용자의 응답 저장
  timeTaken?: number;
}

// ✅ 퀴즈 결과 응답 타입
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
