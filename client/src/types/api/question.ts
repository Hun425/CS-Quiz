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
  options: string[];
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
