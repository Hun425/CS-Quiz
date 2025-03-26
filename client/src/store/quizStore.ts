import { create } from "zustand";

interface QuizStore {
  quizId: number | null;
  attemptId: number | null;
  currentQuestionIndex: number;
  answers: Record<number, string>;
  isQuizCompleted: boolean;
  remainingTime: number;
  questionCount: number;
  setQuiz: (
    quizId: number,
    attemptId: number,
    timeLimit: number,
    questionCount: number
  ) => void;
  setCurrentQuestionIndex: (index: number | ((prev: number) => number)) => void;
  setAnswer: (questionId: number, answer: string) => void;
  decreaseTime: () => void;
  resetQuiz: () => void;
}

export const useQuizStore = create<QuizStore>((set) => ({
  quizId: null,
  attemptId: null,
  currentQuestionIndex: 0,
  answers: {},
  isQuizCompleted: false,
  remainingTime: 0,
  questionCount: 0,

  setQuiz: (quizId, attemptId, timeLimit, questionCount) =>
    set(() => ({
      quizId,
      attemptId,
      remainingTime: timeLimit,
      questionCount,
      currentQuestionIndex: 0,
      answers: {},
      isQuizCompleted: false,
    })),

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

  resetQuiz: () =>
    set(() => ({
      quizId: null,
      attemptId: null,
      currentQuestionIndex: 0,
      answers: {},
      isQuizCompleted: false,
      remainingTime: 0,
      questionCount: 0,
    })),

  decreaseTime: () =>
    set((state) => ({
      remainingTime: state.remainingTime > 0 ? state.remainingTime - 1 : 0,
    })),
}));
