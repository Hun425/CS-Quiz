"use client";

import { useRouter, useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { getPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import Button from "@/app/_components/Button";
import { useSubmitQuiz } from "@/lib/api/quiz/useSubmitQuizResult";
import { useQuizStore } from "@/store/quizStore";
import { QuizPlayResponse } from "@/lib/types/quiz";

const QuizPlayPage: React.FC = () => {
  const router = useRouter();
  const quizId = Number(useParams().id);
  const submitQuizMutation = useSubmitQuiz();

  // ✅ Zustand에서 남은 시간 상태 가져오기
  const { remainingTime, attemptId, setQuiz, decreaseTime } = useQuizStore();
  const [quizPlayData, setQuizPlayData] = useState<QuizPlayResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);
  // ✅ 퀴즈 상태 관리
  const {
    currentQuestionIndex,
    answers,
    isQuizCompleted,
    setCurrentQuestionIndex,
    setAnswer,
    resetQuiz,
  } = useQuizStore();

  useEffect(() => {
    if (!quizPlayData && !attemptId) {
      getPlayableQuiz(quizId).then((data) => {
        if (data?.quizAttemptId) {
          setQuiz(
            quizId,
            data.quizAttemptId,
            data.timeLimit,
            data.questionCount
          ); // ✅ 제한 시간 및 총 문제 개수 설정
          setQuizPlayData(data);
          setIsLoading(false);
        }
      });
    }
  }, [quizId, quizPlayData, setQuiz]);

  // ✅ 퀴즈 제출 핸들러
  const handleSubmitQuiz = async () => {
    if (!quizPlayData) return;

    if (!isQuizCompleted) {
      alert("퀴즈가 완료되지 않았습니다. 모든 문제에 답을 선택해주세요.");
      return;
    }

    try {
      await submitQuizMutation.mutateAsync({
        quizId,
        submitData: {
          quizAttemptId: attemptId!,
          answers,
          timeTaken: quizPlayData.timeLimit - remainingTime,
        },
      });

      router.push(`/quizzes/${quizId}/results?attemptId=${attemptId}`);
    } catch (error) {
      alert("퀴즈 제출 중 오류가 발생했습니다.");
    }
  };

  // ✅ 타이머 관리 (1초마다 감소)
  useEffect(() => {
    if (remainingTime > 0) {
      const timer = setInterval(() => {
        decreaseTime();
      }, 1000);

      return () => clearInterval(timer);
    } else {
      handleSubmitQuiz();
    }
  }, [remainingTime, decreaseTime]);

  useEffect(() => {
    const handleBeforeUnload = () => {
      resetQuiz();
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === "hidden") {
        resetQuiz();
      }
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    document.addEventListener("visibilitychange", handleVisibilityChange);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
      document.removeEventListener("visibilitychange", handleVisibilityChange);
    };
  }, [resetQuiz]);

  // ✅ 로딩 중이면 로딩 UI 표시
  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12 text-xl min-h-screen">
        🔄 퀴즈 정보를 불러오는 중...
      </div>
    );
  }

  // ✅ 데이터가 없으면 에러 UI 표시
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
          className={`text-white shadow-md hover:shadow-lg transition-all ${
            isQuizCompleted ? "" : "opacity-100"
          }`}
        >
          ✅ 제출하기
        </Button>
      </aside>

      <section className="flex-1 min-w-xl max-w-2xl w-full mx-auto p-6 bg-background rounded-lg">
        <div className="flex flex-col gap-6">
          <div className="flex justify-between items-center bg-sub-background p-4 rounded-lg">
            <h2 className="text-2xl font-semibold text-primary">
              문제 {currentQuestionIndex + 1} / {quizPlayData.questions.length}
            </h2>
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
        {/* ✅ 모바일 문제 선택 네비게이션 (점 형태) */}
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

        {/* 버튼 영역 */}
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
};

export default QuizPlayPage;
