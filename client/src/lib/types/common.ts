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

export type DifficultyLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type QuestionType =
  | "MULTIPLE_CHOICE"
  | "TRUE_FALSE"
  | "SHORT_ANSWER"
  | "CODE_ANALYSIS"
  | "DIAGRAM_BASED";
