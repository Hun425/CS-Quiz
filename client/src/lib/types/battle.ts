import { QuestionType } from "./question";

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

// 참가자 정보
export interface Participant {
  userId: number;
  username: string;
  profileImage?: string | null;
  level: number;
  isReady: boolean;
}

export enum BattleStatus {
  WAITING = "WAITING",
  IN_PROGRESS = "IN_PROGRESS",
  FINISHED = "FINISHED",
}

// 배틀룸 정보
export interface BattleRoomResponse {
  id: number;
  quizId: number;
  quizTitle: string;
  status: BattleStatus;
  roomCode: string;
  currentParticipants: number;
  maxParticipants: number;
  questionCount: number;
  timeLimit: number;
  participants: Participant[];
  createdAt: string;
}

// 배틀 참가 응답 (WebSocket)
export interface BattleJoinResponse {
  roomId: number;
  userId: number;
  username: string;
  currentParticipants: number;
  maxParticipants: number;
  participants: Participant[];
  joinedAt: string;
}

// 다음 문제 응답
export interface BattleNextQuestionResponse {
  questionId: number;
  questionText: string;
  questionType: QuestionType;
  options?: string[];
  timeLimit: number;
  points: number;
  isLastQuestion?: boolean;
  isGameOver: boolean;
}

// 배틀 시작 응답 (WebSocket)
export interface BattleStartResponse {
  roomId: number;
  participants: Pick<
    Participant,
    "userId" | "username" | "profileImage" | "level"
  >[];
  totalQuestions: number;
  timeLimit: number;
  startTime: string;
  firstQuestion: BattleNextQuestionResponse;
}

// 답변 제출 요청 (WebSocket)
export interface BattleAnswerRequest {
  roomId: number;
  questionId: number;
  answer: string;
  timeSpentSeconds: number;
}

// 답변 결과 응답
export interface BattleAnswerResponse {
  questionId: number;
  isCorrect: boolean;
  earnedPoints: number;
  timeBonus?: number;
  currentScore?: number;
  correctAnswer: string;
  explanation: string;
}

// 참가자 진행 상황
export interface ParticipantProgress {
  userId: number;
  username: string;
  currentScore: number;
  correctAnswers: number;
  totalAnswered: number;
  hasAnsweredCurrent: boolean;
  currentStreak: number;
}

// 배틀 진행 상황 응답
export interface BattleProgressResponse {
  roomId?: number;
  currentQuestionIndex: number;
  totalQuestions?: number;
  remainingTimeSeconds?: number;
  participantProgress: { [userId: number]: ParticipantProgress };
  status?: "WAITING" | "IN_PROGRESS" | "FINISHED";
}

// 배틀 결과
export interface BattleResult {
  userId: number;
  username: string;
  finalScore: number;
  correctAnswers: number;
  totalQuestions?: number;
  experienceGained: number;
  isWinner: boolean;
  questionResults?: {
    questionId: number;
    isCorrect: boolean;
    earnedPoints: number;
    timeSpentSeconds: number;
  }[];
}

// 배틀 종료 응답
export interface BattleEndResponse {
  roomId?: number;
  results: BattleResult[];
  totalQuestions: number;
  timeTakenSeconds: number;
  endTime: string;
}

// 준비 상태 요청
export interface BattleReadyRequest {
  userId: number;
  roomId: number;
}

// 준비 상태 응답
export interface BattleReadyResponse {
  roomId: number;
  type: string;
  participants: Participant[];
}
