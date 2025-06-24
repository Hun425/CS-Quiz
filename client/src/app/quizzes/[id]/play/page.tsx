"use client";

import { useEffect, useState } from "react";
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
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    attemptId,
    currentQuestionIndex,
    answers,
    isQuizCompleted,
    remainingTime,
    setCurrentQuestionIndex,
    setAnswer,
    // resetQuiz,
    getElapsedTime,
  } = useQuizStore();

  const { quizPlayData, error, isLoading } = useLoadQuizPlayData(quizId);

  // ✅ 뒤로가기 감지 + confirm
  // useEffect(() => {
  //   const handlePopState = () => {
  //     const currentPath = window.location.pathname;
  //     // ❗ "/quizzes/[id]/play"를 벗어나려 할 때만
  //     if (!currentPath.includes(`/quizzes/${id}/play`)) {
  //       const confirmLeave = window.confirm(
  //         "퀴즈가 초기화됩니다. 나가시겠습니까?"
  //       );
  //       if (confirmLeave) {
  //         resetQuiz(true); // 나가기 허용
  //       } else {
  //         setTimeout(() => {
  //           window.history.forward(); // 취소했으면 앞으로 다시 밀기
  //         }, 0);
  //       }
  //     }
  //   };

  //   window.addEventListener("popstate", handlePopState);

  //   return () => {
  //     window.removeEventListener("popstate", handlePopState);
  //     resetQuiz(true); // 컴포넌트 언마운트 시 세션 정리
  //   };
  // }, [id, resetQuiz]);

  // ✅ 새로고침 방지용 beforeunload
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      e.preventDefault();
      e.returnValue = "";
    };
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, []);

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
      setIsSubmitting(true);

      const elapsedTime = getElapsedTime();
      await submitQuizMutation.mutateAsync({
        quizId,
        submitData: {
          quizAttemptId: attemptId!,
          answers,
          timeTaken: elapsedTime,
        },
      });

      router.push(`/quizzes/${quizId}/results?attemptId=${attemptId}`);
    } catch {
      alert("퀴즈 제출 중 오류가 발생했습니다.");
      router.push(`/quizzes/${quizId}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (error) {
    return (
      <div className="flex items-center justify-center text-red-500 py-12 min-h-screen">
        ❌ 퀴즈 로딩 실패
      </div>
    );
  }

  if (isSubmitting) {
    return (
      <div className="flex items-center justify-center min-h-screen text-lg">
        ✍️ 퀴즈를 제출하는 중입니다...
      </div>
    );
  }

  if (isLoading || !quizPlayData) {
    return (
      <div className="flex items-center justify-center min-h-screen text-lg">
        🔄 퀴즈 데이터를 불러오는 중...
      </div>
    );
  }

  return (
    <div className="flex flex-col lg:flex-row min-h-screen bg-sub-background">
      {/* 사이드바 */}
      <aside className="hidden lg:flex flex-col w-64 bg-background rounded-xl p-4 border-r border-border space-y-4">
        <h3 className="text-lg font-bold text-primary mb-4">📌 진행 상황</h3>
        <div className="space-y-2">
          {quizPlayData.questions.map((_, index) => {
            const isSelected = index === currentQuestionIndex;
            const isAnswered = !!answers[quizPlayData.questions[index].id];

            return (
              <button
                key={index}
                className={`w-full px-4 py-2 text-sm rounded-md text-left transition-colors
                  ${
                    isSelected
                      ? "bg-primary text-white"
                      : isAnswered
                      ? "bg-green-500 text-white"
                      : "bg-sub-background hover:bg-muted"
                  }
                `}
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
          className="text-white mt-6"
        >
          제출하기
        </Button>
      </aside>

      {/* 본문 */}
      <section className="flex-1 min-w-xl max-w-2xl w-full mx-auto p-6 bg-background rounded-lg">
        <div className="flex flex-col gap-6">
          {/* 문제 */}
          <div className="flex justify-between items-center bg-sub-background px-4 py-2 rounded-lg">
            <h3 className="text-md font-bold text-primary">
              문제 {currentQuestionIndex + 1} / {quizPlayData.questions.length}
            </h3>
            <Timer onTimeUp={handleSubmitQuiz} />
          </div>

          <div className="text-lg sm:text-xl font-semibold text-foreground leading-relaxed">
            {quizPlayData.questions[currentQuestionIndex].questionText}
          </div>

          {/* 보기 */}
          <div className="space-y-3">
            {quizPlayData.questions[currentQuestionIndex].options.map(
              (option) => {
                const selected =
                  answers[quizPlayData.questions[currentQuestionIndex].id] ===
                  option.value;
                return (
                  <button
                    key={option.key}
                    className={`w-full text-left px-4 py-3 rounded-md border text-base sm:text-lg transition-colors
                    ${
                      selected
                        ? "bg-primary text-white border-primary"
                        : "bg-background hover:bg-muted"
                    }
                  `}
                    onClick={() =>
                      setAnswer(
                        quizPlayData.questions[currentQuestionIndex].id,
                        option.value
                      )
                    }
                  >
                    {option.key}. {option.value}
                  </button>
                );
              }
            )}
          </div>
        </div>

        {/* 모바일용 문제 네비 */}
        <div className="lg:hidden flex justify-center gap-4 my-6">
          {quizPlayData.questions.map((_, index) => {
            const isSelected = index === currentQuestionIndex;
            const isAnswered = !!answers[quizPlayData.questions[index].id];

            return (
              <button
                key={index}
                className={`w-4 h-4 rounded-full transition-colors
                  ${
                    isSelected
                      ? "bg-primary scale-125"
                      : isAnswered
                      ? "bg-green-500"
                      : "bg-gray-300"
                  }
                `}
                onClick={() => setCurrentQuestionIndex(index)}
              />
            );
          })}
        </div>

        {/* 이전/다음 버튼 */}
        <div className="flex justify-between gap-4 mt-8">
          <Button
            disabled={currentQuestionIndex === 0}
            variant="secondary"
            className="w-full md:w-auto"
            onClick={() => setCurrentQuestionIndex((prev) => prev - 1)}
          >
            ⬅ 이전 문제
          </Button>

          {currentQuestionIndex === quizPlayData.questions.length - 1 ? (
            <Button
              variant="primary"
              className="text-white w-full md:w-auto"
              onClick={handleSubmitQuiz}
            >
              제출하기
            </Button>
          ) : (
            <Button
              variant="primary"
              className="text-white w-full md:w-auto"
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
