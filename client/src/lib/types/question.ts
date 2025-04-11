import { QuizDifficultyType } from "./quiz";

/**
 * ✅ 문제 유형 (QuestionType) : 사용자가 퀴즈를 풀 때 API에서 반환하는 문제 데이터에 포함됨
 * - 퀴즈 문제의 유형을 정의하는 열거형(Enum)/Multiple Choice, True/False, Short Answer, Code Analysis, Diagram Based
 *
 */
export enum QuestionType {
  MULTIPLE_CHOICE = "MULTIPLE_CHOICE",
  TRUE_FALSE = "TRUE_FALSE",
  SHORT_ANSWER = "SHORT_ANSWER",
  CODE_ANALYSIS = "CODE_ANALYSIS",
  DIAGRAM_BASED = "DIAGRAM_BASED",
}

/**
 * ✅ 문제 응답 타입 (QuestionResponse)
 * - 사용자가 퀴즈를 풀 때 API에서 반환하는 문제 데이터
 */
export interface QuestionResponse {
  id: number;
  questionType: QuestionType;
  questionText: string;
  codeSnippet?: string;
  diagramData?: string;
  options: { key: string; value: string }[];
  explanation: string;
  points: number;
  difficultyLevel: QuizDifficultyType;
  timeLimitSeconds: number;
}

/**
 * ✅ 문제 생성 요청 타입 (QuestionCreateRequest)
 * - 새로운 문제를 생성할 때 서버로 전달하는 요청 데이터
 * - 권한 필요
 */
export interface QuestionCreateRequest {
  questionType: QuestionType;
  questionText: string;
  codeSnippet?: string;
  diagramData?: string;
  options?: { key: string; value: string }[];
  correctAnswer: string;
  explanation: string;
  points: number;
  difficultyLevel: QuizDifficultyType;
}
