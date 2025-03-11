import { QuestionResponse } from "./question";
import { TagResponse } from "./tag";

export interface QuizDetailResponse {
  id: number;
  title: string;
  description: string;
  quizType: "DAILY" | "TAG_BASED" | "TOPIC_BASED" | "CUSTOM";
  difficultyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
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
  quizType: "DAILY" | "TAG_BASED" | "TOPIC_BASED" | "CUSTOM";
  difficultyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  questionCount: number;
  attemptCount: number;
  avgScore: number;
  tags: TagResponse[];
  createdAt: string;
}

export interface QuizCreateRequest {
  title: string;
  description: string;
  quizType: "TAG_BASED" | "TOPIC_BASED" | "CUSTOM";
  difficultyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  timeLimit: number;
  tagIds: number[];
  questions: QuizCreateRequest[];
}

export interface QuizSearchRequest {
  title?: string;
  difficultyLevel?: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  quizType?: "DAILY" | "TAG_BASED" | "TOPIC_BASED" | "CUSTOM";
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
