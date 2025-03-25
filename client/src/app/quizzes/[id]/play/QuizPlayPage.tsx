"use client";

import { useRouter, useParams } from "next/navigation";
import { useState, useEffect } from "react";
import { useAuthStore } from "@/store/authStore";
import { useQuizTimer } from "@/providers/QuizTimeProvider";
import { useGetPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import Button from "@/app/_components/Button";
import { useSubmitQuiz } from "@/lib/api/quiz/useSubmitQuizResult";
import Timer from "./_components/Timer";

const QuizPlayPage: React.FC = () => {
  const router = useRouter();
  const quizId = useParams().id;
  const { isAuthenticated } = useAuthStore();
  const [authChecked, setAuthChecked] = useState(false);
  const { isLoading, data: quizPlayData } = useGetPlayableQuiz(Number(quizId));
  const attemptId = quizPlayData?.quizAttemptId;
  const submitQuizMutation = useSubmitQuiz();
  const { timeTaken, startTimer, stopTimer } = useQuizTimer();

  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [_quizCompleted, setQuizCompleted] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // ✅ 모든 문제에 답변을 입력했는지 체크
  const isQuizCompleted =
    Object.keys(answers).length === quizPlayData?.questions.length;

  // ✅ 답안 선택 핸들러
  const handleAnswerSelect = (questionId: number, answer: string) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: answer,
    }));
  };

  useEffect(() => {
    startTimer();
    return () => stopTimer();
  }, []);

  const handleSubmitQuiz = async () => {
    if (submitting || !isQuizCompleted) return;
    setSubmitting(true);

    // ✅ 모든 문제를 포함하는 answers 객체 생성
    const allAnswers: Record<number, string> = quizPlayData.questions.reduce(
      (acc, question) => {
        acc[question.id] = answers[question.id] || " "; // 선택하지 않은 문제는 빈 값
        return acc;
      },
      {} as Record<number, string>
    );

    // ✅ 모든 문제 포함된 submitData 전달
    await submitQuizMutation.mutateAsync({
      quizId: Number(quizId),
      submitData: {
        quizAttemptId: attemptId!,
        answers: allAnswers,
        timeTaken,
      },
    });

    setQuizCompleted(true);
    stopTimer();
    router.push(`/quizzes/${quizId}/results`);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12 text-xl min-h-screen">
        🔄 퀴즈 정보를 불러오는 중...
      </div>
    );
  }

  if (!quizPlayData) {
    return (
      <div className="flex justify-center items-center py-12 text-xl min-h-screen text-danger">
        ❌ 퀴즈 데이터를 불러오는 데 실패했습니다.
      </div>
    );
  }

  return (
    <div className="flex flex-col lg:flex-row min-h-screen bg-sub-background">
      {/* 📌 사이드바 (PC 전용) */}
      <aside className="hidden lg:flex flex-col w-64 bg-background shadow-lg rounded-xl p-4 border-r border-border space-y-4">
        <h3 className="text-lg font-semibold text-primary mb-3">
          📌 진행 상황
        </h3>
        <div className="space-y-2">
          {quizPlayData.questions.map((_, index) => (
            <button
              key={index}
              className={`w-full px-4 py-2 text-sm rounded-lg text-left transition-all 
        ${
          index === currentQuestionIndex
            ? "bg-primary text-white shadow-md"
            : answers[quizPlayData.questions[index].id]
            ? "bg-green-500 text-white"
            : "bg-sub-background hover:bg-gray-400"
        }`}
              onClick={() => setCurrentQuestionIndex(index)}
            >
              문제 {index + 1}
            </button>
          ))}
        </div>
        <Button
          variant="primary"
          onClick={handleSubmitQuiz}
          disabled={submitting}
          className={`text-white shadow-md hover:shadow-lg transition-all ${
            isQuizCompleted ? "" : "opacity-100 "
          }`}
        >
          ✅ 제출하기
        </Button>
      </aside>

      {/* ✅ 문제 카드 */}
      <section className="flex-1 min-w-xl max-w-2xl w-full mx-auto p-6 bg-background rounded-lg">
        <div className="flex flex-col gap-6">
          <div className="flex justify-between items-center bg-sub-background p-4 rounded-lg">
            <h2 className="text-2xl font-semibold text-primary">
              문제 {currentQuestionIndex + 1} / {quizPlayData.questions.length}
            </h2>
            <Timer
              initialTime={quizPlayData.timeLimit}
              onTimeUp={handleSubmitQuiz}
            />
          </div>

          {/* ✅ 문제 내용 */}
          <p className="text-lg text-foreground">
            {quizPlayData.questions[currentQuestionIndex].questionText}
          </p>

          {/* ✅ 선택지 목록 */}
          <div className="space-y-4">
            {quizPlayData.questions[currentQuestionIndex].options.map(
              (option) => (
                <button
                  key={option.key}
                  className={`block w-full text-left px-4 py-3 text-lg rounded-lg border border-border  transition-all shadow-sm ${
                    answers[quizPlayData.questions[currentQuestionIndex].id] ===
                    option.value
                      ? "bg-primary text-white border-primary shadow-md"
                      : "bg-sub-background hover:bg-gray-400"
                  }`}
                  onClick={() =>
                    handleAnswerSelect(
                      quizPlayData.questions[currentQuestionIndex].id,
                      option.value
                    )
                  }
                >
                  {option.key}. {option.value}
                </button>
              )
            )}
          </div>

          {/* ✅ 네비게이션 버튼 */}
          <div className="flex justify-between">
            <Button
              disabled={currentQuestionIndex === 0}
              variant="secondary"
              className="shadow-md hover:shadow-lg transition-all"
              onClick={() => setCurrentQuestionIndex((prev) => prev - 1)}
            >
              ⬅ 이전 문제
            </Button>

            {currentQuestionIndex === quizPlayData.questions.length - 1 ? (
              <Button
                variant="primary"
                onClick={handleSubmitQuiz}
                disabled={submitting}
                className={`text-white shadow-md hover:shadow-lg transition-all ${
                  isQuizCompleted ? "" : "opacity-100 "
                }`}
              >
                ✅ 제출하기
              </Button>
            ) : (
              <Button
                variant="primary"
                className="text-white shadow-md hover:shadow-lg transition-all"
                onClick={() => setCurrentQuestionIndex((prev) => prev + 1)}
              >
                다음 문제 ➡
              </Button>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};

export default QuizPlayPage;
