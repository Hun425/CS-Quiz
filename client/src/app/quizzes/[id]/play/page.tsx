"use client";

import { useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useSubmitQuiz } from "@/lib/api/quiz/useSubmitQuizResult";
import { useQuizStore } from "@/store/quizStore";
import Timer from "./_components/Timer";
import Button from "@/app/_components/Button";
import useLoadQuizPlayData from "@/lib/hooks/useLoadQuizPlayData";

export default function QuizPlayPage() {
  const { id } = useParams();
  const quizId = Number(id);
  const router = useRouter();
  const submitQuizMutation = useSubmitQuiz();

  const {
    attemptId,
    currentQuestionIndex,
    answers,
    isQuizCompleted,
    remainingTime,
    setCurrentQuestionIndex,
    setAnswer,
    resetQuiz,
    getElapsedTime,
  } = useQuizStore();

  const { quizPlayData, error, isLoading } = useLoadQuizPlayData(quizId);

  // ✅ 상태 초기화 처리
  useEffect(() => {
    const cleanup = () => resetQuiz();

    window.addEventListener("beforeunload", cleanup);
    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "hidden") cleanup();
    });

    const handlePopState = () => {
      cleanup();
      router.replace("/quizzes");
    };
    window.addEventListener("popstate", handlePopState);

    return () => {
      window.removeEventListener("beforeunload", cleanup);
      document.removeEventListener("visibilitychange", cleanup);
      window.removeEventListener("popstate", handlePopState);
    };
  }, [resetQuiz, router]);

  // ✅ 퀴즈 제출
  const handleSubmitQuiz = async () => {
    if (!quizPlayData) return;

    if (!isQuizCompleted && remainingTime === 0) {
      alert("시간이 초과되었습니다. 퀴즈 목록으로 돌아갑니다.");
      router.push(`/quizzes/${quizId}`);
      return;
    }

    if (!isQuizCompleted) {
      alert("퀴즈가 완료되지 않았습니다. 모든 문제에 답을 선택해주세요.");
      return;
    }

    try {
      const elapsedTime = getElapsedTime();

      await submitQuizMutation.mutateAsync({
        quizId,
        submitData: {
          quizAttemptId: attemptId!,
          answers,
          timeTaken: elapsedTime,
        },
      });

      // 세션 제거
      sessionStorage.removeItem("lastAttempt");
      sessionStorage.removeItem(`quiz-${attemptId}`);

      resetQuiz();
      router.push(`/quizzes/${quizId}/results?attemptId=${attemptId}`);
    } catch {
      alert("퀴즈 제출 중 오류가 발생했습니다.");
      router.push(`/quizzes/${quizId}`);
    }
  };

  // ✅ 로딩 / 에러 상태 처리
  if (error) {
    return (
      <div className="text-red-500 text-center py-12 min-h-screen">
        ❌ 퀴즈 로딩 실패
      </div>
    );
  }

  if (isLoading || !quizPlayData) {
    return (
      <div className="text-center py-12 min-h-screen flex items-center justify-center">
        🔄 퀴즈 데이터를 불러오는 중...
      </div>
    );
  }

  return (
    <div className="flex flex-col lg:flex-row min-h-screen bg-sub-background">
      {/* 사이드바 */}
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
        <Button
          variant="primary"
          onClick={handleSubmitQuiz}
          className="text-white shadow-md hover:shadow-lg transition-all"
        >
          ✅ 제출하기
        </Button>
      </aside>

      {/* 본문 */}
      <section className="flex-1 min-w-xl max-w-2xl w-full mx-auto p-6 bg-background rounded-lg">
        <div className="flex flex-col gap-6">
          <div className="flex justify-between items-center bg-sub-background p-4 rounded-lg">
            <h2 className="text-2xl font-semibold text-primary">
              문제 {currentQuestionIndex + 1} / {quizPlayData.questions.length}
            </h2>
            <Timer onTimeUp={handleSubmitQuiz} />
          </div>

          <p className="text-lg text-foreground">
            {quizPlayData.questions[currentQuestionIndex].questionText}
          </p>

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
                    setAnswer(
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
        </div>

        {/* 모바일용 문제 네비 */}
        <div className="lg:hidden flex justify-center gap-2 my-4">
          {quizPlayData.questions.map((_, index) => {
            const isSelected = index === currentQuestionIndex;
            const isAnswered = !!answers[quizPlayData.questions[index].id];

            return (
              <button
                key={index}
                className={`w-3 h-3 rounded-full transition-all ${
                  isSelected
                    ? "bg-primary scale-125"
                    : isAnswered
                    ? "bg-green-500"
                    : "bg-gray-400"
                }`}
                onClick={() => setCurrentQuestionIndex(index)}
              />
            );
          })}
        </div>

        {/* 이전/다음 버튼 */}
        <div className="flex justify-between gap-4 mt-6">
          <Button
            disabled={currentQuestionIndex === 0}
            variant="secondary"
            className="shadow-md hover:shadow-lg transition-all w-full md:w-auto"
            onClick={() => setCurrentQuestionIndex((prev) => prev - 1)}
          >
            ⬅ 이전 문제
          </Button>

          {currentQuestionIndex === quizPlayData.questions.length - 1 ? (
            <Button
              variant="primary"
              className="text-white shadow-md hover:shadow-lg transition-all w-full md:w-auto"
              onClick={handleSubmitQuiz}
            >
              ✅ 제출하기
            </Button>
          ) : (
            <Button
              variant="primary"
              className="text-white shadow-md hover:shadow-lg transition-all w-full md:w-auto"
              onClick={() => setCurrentQuestionIndex((prev) => prev + 1)}
            >
              다음 문제 ➡
            </Button>
          )}
        </div>
      </section>
    </div>
  );
}
