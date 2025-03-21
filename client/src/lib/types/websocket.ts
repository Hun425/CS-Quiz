import { Participant, BattleStatus } from "./battle";

/**
 * 🔹 WebSocket 이벤트 타입 정의
 * participants: 참가자 리스트
 * start: 배틀 시작 이벤트
 * progress: 배틀 진행 상황
 * end: 배틀 종료 이벤트
 * status: 배틀 상태 변경
 *  */
export interface BattleWebSocketEvents {
  PARTICIPANTS: Participant[]; // 참가자 리스트
  START: { startTime: string }; // 배틀 시작 이벤트
  PROGRESS: { questionId: number; status: string }; // 배틀 진행 상황
  END: { winnerId: number }; // 배틀 종료 이벤트
  STATUS: { status: BattleStatus }; // 배틀 상태 변경
}
