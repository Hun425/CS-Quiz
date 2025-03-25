import { create } from "zustand";
import { persist } from "zustand/middleware";

// ✅ Zustand 상태 저장 (persist 적용)
interface QuizState {
  attemptId: number | null;
  answers: Record<number, string>; // 사용자가 선택한 답변
  timeTaken: number; // 퀴즈 진행 시간
  setAnswer: (questionId: number, answer: string) => void;
  incrementTime: () => void;
  resetQuiz: () => void;
}

export const useQuizStore = create(
  persist<QuizState>(
    (set) => ({
      attemptId: null,
      answers: {},
      timeTaken: 0,
      setAttepmtId: (attemptId: number) => set({ attemptId }),
      setAnswer: (questionId, answer) =>
        set((state) => ({
          answers: { ...state.answers, [questionId]: answer },
        })),
      incrementTime: () => set((state) => ({ timeTaken: state.timeTaken + 1 })),
      resetQuiz: () => set({ attemptId: null, answers: {}, timeTaken: 0 }),
    }),
    { name: "quizplay" }
  )
);
