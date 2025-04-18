/**
import BattleParticipantsList from './../../app/battles/_components/BattleParticipantsList';
 * ✅ WebSocket을 통해 서버로부터 수신 가능한 이벤트 타입
 /** 참가자 목록 (입장/퇴장/준비 상태 포함) */
/** 배틀 시작 이벤트 */
/** 배틀 상태 (WAITING, IN_PROGRESS, FINISHED 등) */
/** 배틀 진행 상황 (정답 제출 이후 참가자 상태 포함) */
/** 다음 문제 도착 */
/** 정답 결과 (개인에게만 전송되는 결과 큐) */
/** 배틀 종료 결과 (최종 순위, 점수 등) */

export enum BattleSocketEventKey {
  PARTICIPANTS = "PARTICIPANTS",
  START = "START",
  STATUS = "STATUS",
  PROGRESS = "PROGRESS",
  NEXT_QUESTION = "NEXT_QUESTION",
  RESULT = "RESULT",
  END = "END",
  ERROR = "ERROR",
  TIMEOUT = "TIMEOUT",
  FORCED_NEXT = "FORCED_NEXT",
}

export interface BattleWebSocketEvents {
  [BattleSocketEventKey.PARTICIPANTS]: BattleParticipantsResponse; // 참가자 목록
  [BattleSocketEventKey.START]: BattleStartResponse;
  [BattleSocketEventKey.STATUS]: { status: BattleStatus };
  [BattleSocketEventKey.PROGRESS]: BattleProgressResponse;
  [BattleSocketEventKey.NEXT_QUESTION]: BattleNextQuestionResponse;
  [BattleSocketEventKey.RESULT]: BattleAnswerResponse;
  [BattleSocketEventKey.END]: BattleEndResponse;
  [BattleSocketEventKey.ERROR]: string;
  [BattleSocketEventKey.TIMEOUT]: string;
  [BattleSocketEventKey.FORCED_NEXT]: string;
}

/**
 * ✅ 배틀룸 참가자 목록 응답 타입 (WebSocket)
 * - 배틀룸에 참가한 사용자들의 정보를 포함
 */
export interface BattleParticipantsResponse {
  participants: Participant[]; // 참가자 목록
  roomId: number; // 배틀룸 ID
  userId?: number; // 참가자 ID
  currentParticipants?: number; // 현재 참가자 수
  joinedAt?: string; // 참가 시간 (ISO 8601 형식)
  maxParticipants?: number; // 최대 참가자 수
  username?: string; // 참가자 이름
  type?: string;
}

/**
 * ✅ 배틀룸 생성 요청 타입
 * - 특정 퀴즈에 대한 배틀룸을 생성할 때 사용
 */
export interface BattleRoomCreateRequest {
  quizId: number; // 배틀에서 사용할 퀴즈 ID
  maxParticipants: number; // 최대 참가자 수 (선택 사항)
}

/**
 * ✅ 배틀 참가 요청 타입 (WebSocket)
 * - 사용자가 배틀룸에 참가할 때 사용
 */
export interface BattleJoinRequest {
  roomId: number;
  userId: number;
}

/**
 * ✅ 참가자 정보 타입
 * - 배틀룸 내 각 참가자의 정보를 포함
 */
export interface Participant {
  userId: number; // 사용자 ID
  username: string; // 사용자 이름
  profileImage?: string | null; // 프로필 이미지 (선택 사항)
  level: number; // 사용자 레벨
  ready: boolean; // 참가자의 준비 상태 여부
}

/**
 * ✅ 배틀 진행 상태
 * - Waiting: 대기 중
 * - In Progress: 진행 중
 * - Finished: 종료됨
 */
export enum BattleStatus {
  WAITING = "WAITING", // 대기 중
  READY = "READY", // 준비 완료
  IN_PROGRESS = "IN_PROGRESS", // 진행 중
  FINISHED = "FINISHED", // 종료됨
}

/**
 * ✅ 배틀룸 정보 타입
 * - 생성된 배틀룸에 대한 정보를 포함
 */
export interface BattleRoomResponse {
  id: number; // 배틀룸 ID
  quizId: number; // 퀴즈 ID
  quizTitle: string; // 퀴즈 제목
  status: BattleStatus; // 배틀 진행 상태
  roomCode: string; // 배틀룸 초대 코드
  currentParticipants: number; // 현재 참가자 수
  maxParticipants: number; // 최대 참가자 수
  questionCount: number; // 배틀에서 출제될 문제 수
  timeLimit: number; // 배틀 시간 제한 (초 단위)
  participants: Participant[]; // 참가자 목록
  createdAt: string; // 배틀룸 생성 시간 (ISO 8601 형식)
}

/**
 * ✅ 배틀 참가 응답 타입 (WebSocket)
 * - 참가자가 배틀룸에 참가했을 때 서버로부터 받는 응답
 */
export interface BattleJoinResponse {
  roomId: number; // 배틀룸 ID
  userId: number; // 참가자 ID
  username: string; // 참가자 이름
  currentParticipants: number; // 현재 참가자 수
  maxParticipants: number; // 최대 참가자 수
  participants: Participant[]; // 현재 배틀룸 참가자 목록
  joinedAt: string; // 참가 시간 (ISO 8601 형식)
  creatorUserId: number; // 배틀룸 생성자 ID
}

/**
 * ✅ 배틀에서 다음 문제 응답 타입
 * - 배틀 진행 중 다음 문제를 받는 응답
 */
export interface BattleNextQuestionResponse {
  questionId: number; // 문제 ID
  questionText: string; // 문제 텍스트
  questionType: string; // 문제 타입 (예: MULTIPLE_CHOICE 등)
  options: string[]; // 선택지 목록
  timeLimit: number; // 제한 시간 (초)
  points: number; // 문제 배점
  isLastQuestion: boolean; // 마지막 문제 여부
  isGameOver: boolean; // 게임 종료 여부
}

/**
 * ✅ 배틀 시작 응답 타입 (WebSocket)
 * - 배틀이 시작될 때 서버에서 보내는 응답
 */
export interface BattleStartResponse {
  firstQuestion: BattleNextQuestionResponse; // 첫 번째 문제 정보
  roomId: number; // 배틀룸 ID
  participants: Participant[]; // 참가자 목록
  startTime: string; // 배틀 시작 시간 (ISO 8601 형식)
  timeLimit: number; // 배틀 전체 시간 제한
  totalQuestions: number; // 총 문제 수
}

/**
 * ✅ 배틀 답변 제출 요청 (WebSocket)
 * - 참가자가 문제에 대한 답을 제출할 때 사용
 */
export interface BattleAnswerRequest {
  roomId: number; // 배틀룸 ID
  questionId: number; // 문제 ID
  answer: string; // 참가자의 답변
  timeSpentSeconds: number; // 해당 문제를 푸는 데 걸린 시간 (초 단위)
}

/**
 * ✅ 배틀 답변 결과 응답 타입
 * - 참가자가 답변을 제출한 후 서버에서 받는 응답
 */
export interface BattleAnswerResponse {
  questionId: number; // 문제 ID
  isCorrect: boolean; // 정답 여부
  earnedPoints: number; // 획득한 점수
  timeBonus?: number; // 시간 보너스 점수 (선택 사항)
  currentScore?: number; // 참가자의 현재 점수
  correctAnswer: string; // 정답
  explanation: string; // 정답 설명
}

/**
 * ✅ 참가자의 배틀 진행 상황 타입
 * - 배틀 중 각 참가자의 현재 진행 상태를 나타냄
 */
export interface ParticipantProgress {
  userId: number; // 참가자 ID (백엔드 필드와 일치)
  username: string; // 참가자 이름
  currentScore: number; // 현재 점수
  correctAnswers: number; // 맞춘 문제 개수
  hasAnsweredCurrent: boolean; // 현재 문제에 답변했는지 여부
  currentStreak: number; // 연속 정답 개수
}
/**
 * ✅ 배틀 진행 상황 응답 타입
 * - 배틀 진행 중 참가자의 상태를 전달하는 응답
 */
export interface BattleProgressResponse {
  roomId: number;
  currentQuestionIndex: number;
  totalQuestions: number;
  remainingTimeSeconds: number;
  participantProgress: { [userId: number]: ParticipantProgress };
  status: BattleStatus; // 배틀 진행 상태
}

/**
 * ✅ 배틀 결과 타입
 * - 배틀이 종료된 후 참가자의 최종 성적을 포함
 */
export interface BattleResult {
  userId: number; // 참가자 ID
  username: string; // 참가자 이름
  finalScore: number; // 최종 점수
  correctAnswers: number; // 맞춘 문제 개수
  totalQuestions?: number; // 총 문제 개수 (선택 사항)
  experienceGained: number; // 획득한 경험치
  isWinner: boolean; // 승리 여부
  questionResults?: {
    questionId: number;
    isCorrect: boolean;
    earnedPoints: number;
    timeSpentSeconds: number;
  }[]; // 개별 문제 결과 (선택 사항)
}

/**
 * ✅ 배틀 종료 응답 타입
 * - 배틀이 종료될 때 서버에서 보내는 응답
 */
export interface BattleEndResponse {
  roomId?: number; // 배틀룸 ID (선택 사항)
  results: BattleResult[]; // 참가자들의 최종 결과
  totalQuestions: number; // 총 문제 개수
  timeTakenSeconds: number; // 총 소요 시간 (초 단위)
  endTime: string; // 배틀 종료 시간 (ISO 8601 형식)
}

/**
 * ✅ 배틀 준비 상태 요청 타입
 * - 참가자가 준비 상태를 변경할 때 사용
 */
export interface BattleReadyRequest {
  userId: number; // 사용자 ID
  roomId: number; // 배틀룸 ID
}

/**
 * ✅ 배틀 준비 상태 응답 타입
 * - 배틀룸 내 참가자의 준비 상태가 변경될 때 서버에서 받는 응답
 */
export interface BattleReadyResponse {
  roomId: number; // 배틀룸 ID
  type: string; // 응답 타입 (예: "READY_UPDATE")
  participants: Participant[]; // 현재 참가자 목록
}
