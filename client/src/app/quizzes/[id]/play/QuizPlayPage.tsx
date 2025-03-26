"use client";

import { useRouter, useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { useQuizTimer } from "@/providers/QuizTimeProvider";
import { getPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import Button from "@/app/_components/Button";
import { useSubmitQuiz } from "@/lib/api/quiz/useSubmitQuizResult";
import { useQuizStore } from "@/store/quizStore";
import Timer from "./_components/Timer";
import { QuizPlayResponse } from "@/lib/types/quiz";

const QuizPlayPage: React.FC = () => {
  const router = useRouter();
  const quizId = Number(useParams().id);
  const submitQuizMutation = useSubmitQuiz();
  const { timeTaken, startTimer, stopTimer } = useQuizTimer();

  // ✅ 퀴즈 데이터를 저장할 상태
  const [quizPlayData, setQuizPlayData] = useState<QuizPlayResponse | null>(
    null
  );

  // ✅ 퀴즈 상태 관리 (attemptId 유지)
  const {
    attemptId,
    currentQuestionIndex,
    answers,
    isQuizCompleted,
    setQuiz,
    setCurrentQuestionIndex,
    setAnswer,
    resetQuiz,
  } = useQuizStore();

  // ✅ `attemptId`가 없을 때만 API 호출 및 상태 업데이트
  useEffect(() => {
    if (!attemptId) {
      getPlayableQuiz(quizId).then((data) => {
        if (data?.quizAttemptId) {
          setQuiz(quizId, data.quizAttemptId);
          setQuizPlayData(data); // ✅ 가져온 데이터 저장
        }
      });
    }
  }, [quizId, attemptId]);

  // ✅ 퀴즈 진행 상태 관리 (창이 닫힐 때 처리)
  useEffect(() => {
    const isOngoingQuiz = attemptId && Object.keys(answers).length > 0;

    const handleUnload = () => {
      if (!isOngoingQuiz) {
        resetQuiz();
      }
    };

    window.addEventListener("beforeunload", handleUnload);
    window.addEventListener("pagehide", handleUnload);

    startTimer();
    return () => {
      stopTimer();
      window.removeEventListener("beforeunload", handleUnload);
      window.removeEventListener("pagehide", handleUnload);
    };
  }, [quizId, attemptId, answers, startTimer, stopTimer, resetQuiz]);

  // ✅ 답변 선택 핸들러
  const handleAnswerSelect = (questionId: number, answer: string) => {
    setAnswer(questionId, answer);
  };

  // ✅ 퀴즈 제출 핸들러
  const handleSubmitQuiz = async () => {
    if (!quizPlayData) return;

    console.log("Attempting to submit quiz:", {
      isQuizCompleted,
      answersCount: Object.keys(answers).length,
      totalQuestions: quizPlayData?.questions.length,
      answers,
    });

    if (!isQuizCompleted) {
      alert("퀴즈가 완료되지 않았습니다. 모든 문제에 답을 선택해주세요.");
      return;
    }

    try {
      console.log("제출될 quiz data:", {
        quizId,
        attemptId,
        answers,
        timeTaken,
      });

      const result = await submitQuizMutation.mutateAsync({
        quizId,
        submitData: {
          quizAttemptId: attemptId!,
          answers,
          timeTaken,
        },
      });

      console.log("Submit mutation result:", result);

      router.push(`/quizzes/${quizId}/results?attemptId=${attemptId}`);
    } catch (error) {
      console.error("Quiz submission error:", error);
      alert("퀴즈 제출 중 오류가 발생했습니다.");
    }
  };

  // ✅ 데이터 로딩 중 상태
  if (!quizPlayData) {
    return (
      <div className="flex justify-center items-center py-12 text-xl min-h-screen">
        🔄 퀴즈 정보를 불러오는 중...
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
          {quizPlayData.questions.map((_, index) => {
            const isSelected = index === currentQuestionIndex;
            const isAnswered = !!answers[quizPlayData.questions[index].id];

            return (
              <button
                key={index}
                className={`w-full px-4 py-2 text-sm rounded-lg text-left transition-all ${
                  isSelected
                    ? "bg-primary text-white shadow-md"
                    : isAnswered
                    ? "bg-green-500 text-white"
                    : "bg-sub-background hover:bg-gray-400"
                }`}
                onClick={() => setCurrentQuestionIndex(index)}
              >
                문제 {index + 1}
              </button>
            );
          })}
        </div>
        <Button variant="primary" onClick={handleSubmitQuiz}>
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
                  className={`block w-full text-left px-4 py-3 text-lg rounded-lg border border-border transition-all shadow-sm ${
                    answers[quizPlayData.questions[currentQuestionIndex].id] ===
                    option.key
                      ? "bg-primary text-white border-primary shadow-md"
                      : "bg-sub-background hover:bg-gray-400"
                  }`}
                  onClick={() =>
                    handleAnswerSelect(
                      quizPlayData.questions[currentQuestionIndex].id,
                      option.key
                    )
                  }
                >
                  {option.key}. {option.value}
                </button>
              )
            )}
          </div>

          {/* ✅ 문제 진행 상태를 점으로 표시 (모바일 전용) */}
          <div className="lg:hidden flex justify-center gap-2 mb-4">
            {quizPlayData.questions.map((_, index) => {
              const isSelected = index === currentQuestionIndex;
              const isAnswered = !!answers[quizPlayData.questions[index].id];

              return (
                <span
                  key={index}
                  className={`w-3 h-3 rounded-full transition-all ${
                    isSelected
                      ? "bg-primary scale-125"
                      : isAnswered
                      ? "bg-green-500"
                      : "bg-gray-500"
                  }`}
                />
              );
            })}
          </div>
        </div>
        {/* ✅ 네비게이션 버튼 (모바일에서 더 넓게) */}
        <div className="flex justify-between gap-4 mt-6">
          <Button
            disabled={currentQuestionIndex === 0}
            variant="secondary"
            className="shadow-md hover:shadow-lg transition-all w-full md:w-auto"
            onClick={() => setCurrentQuestionIndex((prev) => prev - 1)}
          >
            ⬅ 이전 문제
          </Button>

          <Button
            variant="primary"
            className="text-white shadow-md hover:shadow-lg transition-all w-full md:w-auto"
            onClick={
              currentQuestionIndex === quizPlayData.questions.length - 1
                ? handleSubmitQuiz
                : () => setCurrentQuestionIndex((prev) => prev + 1)
            }
          >
            {currentQuestionIndex === quizPlayData.questions.length - 1
              ? "✅ 제출하기"
              : "다음 문제 ➡"}
          </Button>
        </div>
      </section>
    </div>
  );
};

export default QuizPlayPage;
