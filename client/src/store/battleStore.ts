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

  setParticipantsPayload: (data: BattleParticipantsPayload) => void;
  setStartPayload: (data: BattleStartResponse) => void;
  setStatus: (status: BattleStatus) => void;
  setProgress: (data: BattleProgressResponse) => void;
  setNextQuestion: (data: BattleNextQuestionResponse) => void;
  setResult: (data: BattleAnswerResponse) => void;
  setEndPayload: (data: BattleEndResponse) => void;
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

  setParticipantsPayload: (data) => set({ participantsPayload: data }),
  setStartPayload: (data) => set({ startPayload: data }),
  setStatus: (status) => set({ status }),
  setProgress: (data) => set({ progress: data }),
  setNextQuestion: (data) => set({ nextQuestion: data }),
  setResult: (data) => set({ result: data }),
  setEndPayload: (data) => set({ endPayload: data }),

  reset: () =>
    set({
      participantsPayload: null,
      startPayload: null,
      status: null,
      progress: null,
      nextQuestion: null,
      result: null,
      endPayload: null,
    }),
}));
