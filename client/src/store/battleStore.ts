import { create } from "zustand";
import { persist } from "zustand/middleware";

import {
  Participant,
  BattleStartResponse,
  BattleStatus,
  BattleProgressResponse,
  BattleNextQuestionResponse,
  BattleAnswerResponse,
  BattleEndResponse,
} from "@/lib/types/battle";

interface BattleSocketState {
  participantsPayload: Participant[] | null;
  startPayload: BattleStartResponse | null;
  status: BattleStatus | null;
  progress: BattleProgressResponse | null;
  nextQuestion: BattleNextQuestionResponse | null;
  result: BattleAnswerResponse | null;
  endPayload: BattleEndResponse | null;
  lastUpdatedAt: number | null;

  setParticipantsPayload: (data: Participant[]) => void;
  setStartPayload: (data: BattleStartResponse) => void;
  setStatus: (status: BattleStatus) => void;
  setProgress: (data: BattleProgressResponse) => void;
  setNextQuestion: (data: BattleNextQuestionResponse) => void;
  setResult: (data: BattleAnswerResponse) => void;
  setEndPayload: (data: BattleEndResponse) => void;
  updateLastActivity: () => void;
  reset: () => void;
}
export const useBattleSocketStore = create<BattleSocketState>()(
  persist(
    (set) => ({
      participantsPayload: null,
      startPayload: null,
      status: null,
      progress: null,
      nextQuestion: null,
      result: null,
      endPayload: null,
      lastUpdatedAt: Date.now(),

      setParticipantsPayload: (data) =>
        set({ participantsPayload: data, lastUpdatedAt: Date.now() }),
      setStartPayload: (data) =>
        set({ startPayload: data, lastUpdatedAt: Date.now() }),
      setStatus: (status) => set({ status, lastUpdatedAt: Date.now() }),
      setProgress: (data) => set({ progress: data, lastUpdatedAt: Date.now() }),
      setNextQuestion: (data) =>
        set({ nextQuestion: data, lastUpdatedAt: Date.now() }),
      setResult: (data) => set({ result: data, lastUpdatedAt: Date.now() }),
      setEndPayload: (data) =>
        set({ endPayload: data, lastUpdatedAt: Date.now() }),
      updateLastActivity: () => set({ lastUpdatedAt: Date.now() }),

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
    }),
    {
      name: "battle-socket-store",
      storage: {
        getItem: (name) => {
          const value = sessionStorage.getItem(name);
          return value ? JSON.parse(value) : null;
        },
        setItem: (name, value) => {
          sessionStorage.setItem(name, JSON.stringify(value));
        },
        removeItem: (name) => {
          sessionStorage.removeItem(name);
        },
      },
    }
  )
);
