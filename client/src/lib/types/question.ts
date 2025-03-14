import { QuizDifficultyType } from "./quiz";

export enum QuestionType {
  MULTIPLE_CHOICE = "MULTIPLE_CHOICE",
  TRUE_FALSE = "TRUE_FALSE",
  SHORT_ANSWER = "SHORT_ANSWER",
  CODE_ANALYSIS = "CODE_ANALYSIS",
  DIAGRAM_BASED = "DIAGRAM_BASED",
}

export interface QuestionResponse {
  id: number;
  questionType: QuestionType;
  questionText: string;
  codeSnippet?: string;
  diagramData?: string;
  options: string[];
  explanation: string;
  points: number;
  difficultyLevel: QuizDifficultyType;
  timeLimitSeconds: number;
}

export interface QuestionCreateRequest {
  questionType: QuestionType;
  questionText: string;
  codeSnippet?: string;
  diagramData?: string;
  options?: string[];
  correctAnswer: string;
  explanation: string;
  points: number;
  difficultyLevel: QuizDifficultyType;
}
