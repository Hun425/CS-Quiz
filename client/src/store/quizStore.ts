import { create } from "zustand";
import { QuizPlayResponse } from "@/lib/types/quiz";

interface QuizStore {
  quizId: number | null;
  attemptId: number | null;
  currentQuestionIndex: number;
  answers: Record<number, string>;
  isQuizCompleted: boolean;
  remainingTime: number;
  questionCount: number;
  startTime: number | 0;
  endTime: number | null;
  quizPlayData: QuizPlayResponse | null;

  setQuizPlayData: (data: QuizPlayResponse) => void;
  setQuiz: (
    quizId: number,
    attemptId: number,
    timeLimit: number,
    questionCount: number,
    startTime: number,
    endTime: number
  ) => void;
  setCurrentQuestionIndex: (index: number | ((prev: number) => number)) => void;
  setAnswer: (questionId: number, answer: string) => void;
  resetQuiz: (clearSession?: boolean) => void;
  getElapsedTime: () => number;
}

export const useQuizStore = create<QuizStore>((set, get) => ({
  quizId: null,
  attemptId: null,
  currentQuestionIndex: 0,
  answers: {},
  isQuizCompleted: false,
  remainingTime: 0,
  questionCount: 0,
  startTime: 0,
  endTime: null,
  quizPlayData: null,

  setQuizPlayData: (data) => set({ quizPlayData: data }),

  setQuiz: (
    quizId,
    attemptId,
    timeLimit,
    questionCount,
    startTime,
    endTime
  ) => {
    set(() => ({
      quizId,
      attemptId,
      startTime,
      endTime,
      remainingTime: timeLimit,
      questionCount,
      currentQuestionIndex: 0,
      answers: {},
      isQuizCompleted: false,
    }));
  },

  setCurrentQuestionIndex: (index) =>
    set((state) => ({
      currentQuestionIndex:
        typeof index === "function" ? index(state.currentQuestionIndex) : index,
    })),

  setAnswer: (questionId, answer) =>
    set((state) => {
      const updatedAnswers = { ...state.answers, [questionId]: answer };
      const isCompleted =
        Object.keys(updatedAnswers).length === state.questionCount;

      return {
        answers: updatedAnswers,
        isQuizCompleted: isCompleted,
      };
    }),

  resetQuiz: (clearSession = true) => {
    const { quizId, attemptId } = get();

    if (clearSession && quizId && attemptId) {
      const key = `quiz-${quizId}-${attemptId}`;
      sessionStorage.removeItem("lastAttempt");
      sessionStorage.removeItem(key);
    }

    set({
      quizId: null,
      attemptId: null,
      currentQuestionIndex: 0,
      answers: {},
      isQuizCompleted: false,
      remainingTime: 0,
      questionCount: 0,
      startTime: 0,
      endTime: null,
      quizPlayData: null,
    });
  },

  getElapsedTime: () => {
    const start = get().startTime;
    return start ? Math.floor((Date.now() - start) / 1000) : 0;
  },
}));
