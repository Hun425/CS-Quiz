import { QuestionResponse } from "./question";
import { TagResponse } from "./tag";

// ✅ 문제 관련 타입
export interface QuestionResponse {
  id: number;
  questionType:
    | "MULTIPLE_CHOICE"
    | "TRUE_FALSE"
    | "SHORT_ANSWER"
    | "CODE_ANALYSIS"
    | "DIAGRAM_BASED";
  questionText: string;
  codeSnippet?: string;
  diagramData?: string;
  options?: string[];
  explanation: string;
  points: number;
  difficultyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  timeLimitSeconds: number;
}

export interface QuestionCreateRequest {
  questionType:
    | "MULTIPLE_CHOICE"
    | "TRUE_FALSE"
    | "SHORT_ANSWER"
    | "CODE_ANALYSIS"
    | "DIAGRAM_BASED";
  questionText: string;
  codeSnippet?: string;
  diagramData?: string;
  options?: string[];
  correctAnswer: string;
  explanation: string;
  points: number;
  difficultyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
}

// ✅ 퀴즈 상세 조회 타입
export interface QuizDetailResponse {
  id: number;
  title: string;
  description: string;
  quizType: "DAILY" | "TAG_BASED" | "TOPIC_BASED" | "CUSTOM";
  difficultyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  timeLimit: number;
  questionCount: number;
  tags: TagResponse[]; // ✅ 태그 필드 추가
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

// ✅ 퀴즈 응답 타입 (전체 퀴즈 데이터)
export interface QuizResponse extends QuizDetailResponse {
  questions: QuestionResponse[];
  quizAttemptId?: number;
}

// ✅ 퀴즈 요약 타입 (리스트에서 사용)
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

// ✅ 퀴즈 생성 요청 타입
export interface QuizCreateRequest {
  title: string;
  description: string;
  quizType: "TAG_BASED" | "TOPIC_BASED" | "CUSTOM"; // ✅ DAILY 제외
  difficultyLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  timeLimit: number;
  tagIds: number[];
  questions: QuestionCreateRequest[];
}

// ✅ 퀴즈 검색 요청 타입
export interface QuizSearchRequest {
  title?: string;
  difficultyLevel?: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  quizType?: "DAILY" | "TAG_BASED" | "TOPIC_BASED" | "CUSTOM";
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
