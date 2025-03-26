import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

interface QuizState {
  attemptId: number | null;
  quizId: number | null;
  currentQuestionIndex: number;
  isQuizCompleted: boolean;
  answers: Record<number, string>;
  startTime: number | null;
  setQuiz: (quizId: number, attemptId: number) => void;
  setCurrentQuestionIndex: (
    indexOrUpdater: number | ((prev: number) => number)
  ) => void;
  setAnswer: (questionId: number, answer: string) => void;
  resetQuiz: () => void;
}

export const useQuizStore = create<QuizState>()(
  persist(
    (set, get) => ({
      attemptId: null,
      quizId: null,
      currentQuestionIndex: 0,
      answers: {},
      startTime: null,
      isQuizCompleted: false,

      // ✅ 퀴즈 시작 시 기존 attemptId가 같다면 초기화 X
      setQuiz: (quizId, attemptId) => {
        if (get().attemptId === attemptId) return;
        set({
          quizId,
          attemptId,
          currentQuestionIndex: 0,
          answers: {},
          startTime: get().startTime ?? Date.now(),
          isQuizCompleted: false,
        });
      },

      // ✅ 현재 문제 번호 업데이트
      setCurrentQuestionIndex: (indexOrUpdater) =>
        set((state) => ({
          currentQuestionIndex:
            typeof indexOrUpdater === "function"
              ? indexOrUpdater(state.currentQuestionIndex)
              : indexOrUpdater,
        })),

      // ✅ 사용자 답변 저장 & 퀴즈 완료 상태 업데이트
      setAnswer: (questionId, answer) =>
        set((state) => {
          const updatedAnswers = { ...state.answers, [questionId]: answer };
          const allAnswered =
            Object.values(updatedAnswers).length >= (state.quizId ? 10 : 0);
          return {
            answers: updatedAnswers,
            isQuizCompleted: allAnswered,
          };
        }),

      // ✅ 창이 닫히거나 다른 페이지로 이동하면 초기화
      resetQuiz: () =>
        set({
          attemptId: null,
          quizId: null,
          currentQuestionIndex: 0,
          answers: {},
          startTime: null,
          isQuizCompleted: false,
        }),
    }),
    {
      name: "quiz-session",
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);
