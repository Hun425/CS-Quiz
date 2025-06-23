import { create } from "zustand";
import { QuizPlayResponse } from "@/lib/types/quiz";

interface QuizStore {
  // 상태
  quizId: number | null;
  attemptId: number | null;
  currentQuestionIndex: number;
  answers: Record<number, string>;
  isQuizCompleted: boolean;
  remainingTime: number;
  questionCount: number;
  startTime: number;
  endTime: number | null;
  quizPlayData: QuizPlayResponse | null;

  // 상태 설정 함수
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

  // 초기화 및 유틸
  resetQuiz: (clearSession?: boolean) => void;
  getElapsedTime: () => number;
}

export const useQuizStore = create<QuizStore>((set, get) => ({
  // 초기 상태
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

  // 전체 데이터 세팅 (시작 시점에만 호출)
  setQuiz: (
    quizId,
    attemptId,
    timeLimit,
    questionCount,
    startTime,
    endTime
  ) => {
    set({
      quizId,
      attemptId,
      currentQuestionIndex: 0,
      answers: {},
      isQuizCompleted: false,
      remainingTime: timeLimit,
      questionCount,
      startTime,
      endTime,
    });
  },

  // 퀴즈 플레이 전체 응답 데이터 저장
  setQuizPlayData: (data) => {
    set({ quizPlayData: data });
  },

  // 문제 인덱스 이동
  setCurrentQuestionIndex: (index) =>
    set((state) => ({
      currentQuestionIndex:
        typeof index === "function" ? index(state.currentQuestionIndex) : index,
    })),

  // 답변 등록 및 완료 여부 체크
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

  // 상태 초기화 및 세션 제거
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

  // 경과 시간 계산
  getElapsedTime: () => {
    const start = get().startTime;
    return start ? Math.floor((Date.now() - start) / 1000) : 0;
  },
}));
