"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useState, useEffect } from "react";
import { useAuthStore } from "@/store/authStore";
import { mockQuizPlay } from "@/lib/mockQuizPlay";
import { getPlayableQuiz } from "@/lib/api/quiz/getPlayableQuiz";
import { format } from "date-fns"; // ✅ date-fns 사용

const QuizPlayPage: React.FC = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const quizId = searchParams.get("quizId");
  const { isAuthenticated } = useAuthStore();

  // ✅ 환경 변수 확인 후 더미 데이터 적용
  const useMockData = process.env.NEXT_PUBLIC_USE_MOCK_DATA === "true";
  const {
    isLoading,
    error,
    data: quiz,
  } = useMockData
    ? { isLoading: false, error: null, data: mockQuizPlay }
    : getPlayableQuiz(Number(quizId));

  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [timeLeft, setTimeLeft] = useState(quiz?.timeLimit * 60 || 0);
  const [quizStarted, setQuizStarted] = useState(false);
  const [quizCompleted, setQuizCompleted] = useState(false);
  const [startTime, setStartTime] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push(`/login?redirect=/quizzes/${quizId}/play`);
    }
  }, [isAuthenticated, router, quizId]);

  useEffect(() => {
    if (!quizStarted || quizCompleted || timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          handleSubmitQuiz();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [quizStarted, quizCompleted, timeLeft]);

  const handleStartQuiz = () => {
    setQuizStarted(true);
    setStartTime(Date.now());
  };

  const handleAnswerSelect = (questionId: number, answer: string) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: answer,
    }));
  };

  const handleSubmitQuiz = () => {
    if (!quiz || submitting) return;
    setSubmitting(true);
    setQuizCompleted(true);

    // 🚀 결과 페이지 이동 (모의 데이터 사용)
    router.push(`/quizzes/${quizId}/results`);
  };

  // ✅ 남은 시간 포맷팅 (00:00 형식)
  const formatTimeLeft = () => {
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(
      2,
      "0"
    )}`;
  };

  // ✅ 퀴즈 시작 가능 시간 포맷팅
  const formattedStartTime = format(new Date(), "yyyy.MM.dd HH:mm");

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        🔄 퀴즈 정보를 불러오는 중...
      </div>
    );
  }

  if (error || !quiz) {
    return (
      <div className="max-w-4xl bg-danger-light min-h-screen mx-auto p-6 flex items-center justify-center rounded-lg shadow-lg">
        <p className="text-xl font-semibold text-danger">
          ❌ 퀴즈 정보를 불러올 수 없습니다.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-lg rounded-xl min-h-screen">
      {!quizStarted ? (
        <div className="text-center">
          <h1 className="text-3xl font-bold">{quiz.title}</h1>
          <p className="text-gray-700 mt-2">{quiz.description}</p>
          <p className="mt-2 text-sm text-muted">
            ⏳ 시작 가능 시간: {formattedStartTime}
          </p>
          <button
            onClick={handleStartQuiz}
            className="mt-6 w-full bg-primary hover:bg-primary-hover text-white py-3 rounded-lg font-semibold text-lg"
          >
            🚀 퀴즈 시작하기
          </button>
        </div>
      ) : (
        <div>
          {/* 퀴즈 타이머 */}
          <div className="flex justify-between items-center bg-gray-100 p-4 rounded-lg shadow">
            <h2 className="text-xl font-semibold">{quiz.title}</h2>
            <span className="px-4 py-2 bg-red-500 text-white rounded-md text-lg font-bold">
              ⏳ {formatTimeLeft()}
            </span>
          </div>

          {/* 문제 출력 */}
          <div className="mt-6">
            <h3 className="text-lg font-bold">
              문제 {currentQuestionIndex + 1} / {quiz.questions.length}
            </h3>
            <p className="text-gray-700 mt-2">
              {quiz.questions[currentQuestionIndex].questionText}
            </p>

            <div className="mt-4 space-y-2">
              {quiz.questions[currentQuestionIndex].options.map(
                (option, index) => (
                  <button
                    key={index}
                    className={`block w-full text-left px-4 py-2 rounded-lg border ${
                      answers[quiz.questions[currentQuestionIndex].id] ===
                      option
                        ? "bg-blue-500 text-white border-blue-500"
                        : "bg-gray-50 border-gray-300"
                    }`}
                    onClick={() =>
                      handleAnswerSelect(
                        quiz.questions[currentQuestionIndex].id,
                        option
                      )
                    }
                  >
                    {option}
                  </button>
                )
              )}
            </div>
          </div>

          {/* 네비게이션 */}
          <div className="mt-6 flex justify-between">
            <button
              disabled={currentQuestionIndex === 0}
              className="px-4 py-2 bg-gray-300 rounded-lg"
              onClick={() => setCurrentQuestionIndex((prev) => prev - 1)}
            >
              ⬅ 이전 문제
            </button>
            {currentQuestionIndex < quiz.questions.length - 1 ? (
              <button
                className="px-4 py-2 bg-primary text-white rounded-lg"
                onClick={() => setCurrentQuestionIndex((prev) => prev + 1)}
              >
                다음 문제 ➡
              </button>
            ) : (
              <button
                className="px-4 py-2 bg-green-500 text-white rounded-lg"
                onClick={handleSubmitQuiz}
              >
                ✅ 퀴즈 제출하기
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default QuizPlayPage;
