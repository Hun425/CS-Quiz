import { create } from "zustand";
import {
  BattleParticipantsPayload,
  BattleStartResponse,
  BattleStatus,
  BattleProgressResponse,
  BattleNextQuestionResponse,
  BattleAnswerResponse,
  BattleEndResponse,
} from "@/lib/types/battle";

interface BattleSocketState {
  participantsPayload: BattleParticipantsPayload | null;
  startPayload: BattleStartResponse | null;
  status: BattleStatus | null;
  progress: BattleProgressResponse | null;
  nextQuestion: BattleNextQuestionResponse | null;
  result: BattleAnswerResponse | null;
  endPayload: BattleEndResponse | null;
  lastUpdatedAt: number | null; // 🆕 마지막 응답 시간

  setParticipantsPayload: (data: BattleParticipantsPayload) => void;
  setStartPayload: (data: BattleStartResponse) => void;
  setStatus: (status: BattleStatus) => void;
  setProgress: (data: BattleProgressResponse) => void;
  setNextQuestion: (data: BattleNextQuestionResponse) => void;
  setResult: (data: BattleAnswerResponse) => void;
  setEndPayload: (data: BattleEndResponse) => void;
  updateLastActivity: () => void; // 🆕 수동으로 최근 시간 갱신
  reset: () => void;
}

export const useBattleSocketStore = create<BattleSocketState>((set) => ({
  participantsPayload: null,
  startPayload: null,
  status: null,
  progress: null,
  nextQuestion: null,
  result: null,
  endPayload: null,
  lastUpdatedAt: Date.now(), // 🆕 초기값 현재 시간으로

  setParticipantsPayload: (data) =>
    set({ participantsPayload: data, lastUpdatedAt: Date.now() }),
  setStartPayload: (data) =>
    set({ startPayload: data, lastUpdatedAt: Date.now() }),
  setStatus: (status) => set({ status, lastUpdatedAt: Date.now() }),
  setProgress: (data) => set({ progress: data, lastUpdatedAt: Date.now() }),
  setNextQuestion: (data) =>
    set({ nextQuestion: data, lastUpdatedAt: Date.now() }),
  setResult: (data) => set({ result: data, lastUpdatedAt: Date.now() }),
  setEndPayload: (data) => set({ endPayload: data, lastUpdatedAt: Date.now() }),
  updateLastActivity: () => set({ lastUpdatedAt: Date.now() }), // 수동 갱신용

  reset: () =>
    set({
      participantsPayload: null,
      startPayload: null,
      status: null,
      progress: null,
      nextQuestion: null,
      result: null,
      endPayload: null,
      lastUpdatedAt: Date.now(),
    }),
}));
