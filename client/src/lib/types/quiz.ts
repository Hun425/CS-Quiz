import { QuestionResponse, QuestionCreateRequest } from "./question";
import { TagResponse } from "./tag";

/**
 * ✅ 퀴즈 유형 (QuizType)
 * - 퀴즈의 분류를 정의하는 열거형(Enum)
 */
export enum QuizType {
  REGULAR = "REGULAR",
  DAILY = "DAILY",
  WEEKLY = "WEEKLY",
  SPECIAL = "SPECIAL",
  BATTLE = "BATTLE",
}

/**
 * ✅ 퀴즈 난이도 유형 (QuizDifficultyType)
 * - 퀴즈의 난이도를 정의하는 열거형(Enum)
 */
export enum QuizDifficultyType {
  BEGINNER = "BEGINNER",
  INTERMEDIATE = "INTERMEDIATE",
  ADVANCED = "ADVANCED",
}

/**
 * ✅ 태그생성 유형 (TagCreateType)
 */
export interface TagCreateType {
  name: string;
  description: string;
  parentId?: number;
  synonyms?: string[];
}

/**
 * ✅ 퀴즈 검색 요청 타입 (QuizSearchRequest)
 * - 퀴즈 검색 시 필터링할 수 있는 옵션을 포함
 */
export interface QuizSearchRequest {
  title?: string;
  difficultyLevel?: QuizDifficultyType;
  quizType?: QuizType;
  tagIds?: number[];
  minQuestions?: number;
  maxQuestions?: number;
  orderBy?: "title" | "difficultyLevel" | "quizType" | "createdAt"; // ✅ 안전한 값만 허용
}

//--------------------- 퀴즈 문제 풀이 관련 타입--------------------------------------------

/**
 * ✅ 퀴즈 응답 타입 (QuizResponse)
 * - 퀴즈의 전체 데이터를 포함 (문제 포함)
 * - daily 퀴즈 조회시에도 사용
 */
export interface QuizPlayResponse extends QuizDetailResponse {
  questions: QuestionResponse[];
  quizAttemptId: number;
}

/**
 * ✅ 퀴즈 상세 조회 응답 타입 (QuizDetailResponse)
 * - 개별 퀴즈의 상세 정보를 포함
 */
export interface QuizDetailResponse {
  id: number;
  title: string;
  description: string;
  quizType: QuizType;
  difficultyLevel: QuizDifficultyType;
  timeLimit: number;
  questionCount: number;
  tags: TagResponse[];
  creator: Creator;
  statistics?: QuizStatisticsResponse;
  createdAt: string;
}

/**
 * ✅ 개별 문제 통계 (QuestionStatistics)
 * - 각 문제별 정답률 및 시도 횟수 등의 통계 정보
 */
export interface QuestionStatistics {
  questionId: number;
  correctAnswers: number;
  totalAttempts: number;
  correctRate: number;
  averageTimeSeconds: number;
}

/**
 * ✅ 퀴즈 생성자
 * - 퀴즈를 생성한 사용자 정보
 */
export interface Creator {
  id: number;
  username: string;
  profileImage: string | null;
  level: number;
  joinedAt: string;
}

/**
 * ✅ 퀴즈 통계 응답 타입 (QuizStatisticsResponse)
 * - 특정 퀴즈 또는 전체 퀴즈에 대한 통계 정보를 포함
 * - `totalAttempts`: 전체 시도 횟수
 * - `averageScore`: 평균 점수
 * - `completionRate`: 퀴즈 완료율 (0~100%)
 * - `averageTimeSeconds`: 평균 소요 시간 (초)
 * - `difficultyDistribution`: 난이도별 분포 (키-값 형태)
 * - `questionStatistics`: 개별 문제에 대한 통계 정보
 */
export interface QuizStatisticsResponse {
  totalAttempts: number;
  averageScore: number;
  completionRate: number;
  averageTimeSeconds: number;
  difficultyDistribution: Partial<Record<QuizDifficultyType, number>>;
  questionStatistics: QuestionStatistics[];
}

/**
 * ✅ 퀴즈 요약 응답 타입 (QuizSummaryResponse)
 * - 퀴즈 목록에서 개별 퀴즈를 표시할 때 사용
 * - 추천 퀴즈 반환값
 */
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

/**
 * ✅ 퀴즈 생성 요청 타입 (QuizCreateRequest)
 * - 새로운 퀴즈를 생성할 때 서버로 전달하는 요청 데이터
 */
export interface QuizCreateRequest {
  title: string;
  description: string;
  quizType: QuizType;
  difficultyLevel: QuizDifficultyType;
  timeLimit: number;
  tagIds: number[];
  questions: QuestionCreateRequest[];
}

/**
 * ✅ 퀴즈 제출 요청 타입 (QuizSubmitRequest)
 * - 사용자가 퀴즈를 풀고 제출할 때 서버로 전달하는 데이터
 */
export interface QuizSubmitRequest {
  quizAttemptId: number;
  answers: Record<number, string>;
  timeTaken?: number;
}

/**
 * ✅ 퀴즈 결과 응답 타입 (QuizResultResponse)
 * - 사용자가 퀴즈를 완료한 후 반환되는 결과 데이터
 */
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
