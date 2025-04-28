"use client";

import { useSearchParams, useParams, useRouter } from "next/navigation";
import { useEffect } from "react";
import { useGetQuizResult } from "@/lib/api/quiz/useGetQuizResult";
import { useQuizStore } from "@/store/quizStore";
import Button from "@/app/_components/Button";
import RetryQuizButton from "../play/_components/RetryQuizButton";

const QuizResultPage: React.FC = () => {
  const searchParams = useSearchParams();
  const router = useRouter();
  const quizId = Number(useParams().id);
  const attemptId = searchParams.get("attemptId");

  useEffect(() => {
    useQuizStore.getState().resetQuiz(true);

    if (!attemptId) {
      alert("잘못된 접근입니다.");
      router.replace("/quizzes");
    }
  }, [attemptId, router]);

  // ✅ 퀴즈 결과 조회
  const {
    data: quizResult,
    isLoading,
    error,
  } = useGetQuizResult(quizId, Number(attemptId));

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        🔄 결과 불러오는 중...
      </div>
    );
  }

  if (error || !quizResult) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen">
        <p className="text-red-500">❌ 결과를 불러오는 중 오류 발생</p>
        <Button variant="secondary" onClick={() => router.push("/quizzes")}>
          메인으로 돌아가기
        </Button>
      </div>
    );
  }

  const {
    title,
    totalQuestions,
    correctAnswers,
    score,
    totalPossibleScore,
    timeTaken,
    newTotalExperience, // ✅ 총 경험치
    questions,
  } = quizResult;

  // ✅ 정답률 계산
  const correctPercentage = (correctAnswers / totalQuestions) * 100;

  return (
    <div className="min-w-xl max-w-3xl mx-auto p-6 ">
      {/* ✅ 퀴즈 요약 (한 줄 정리) */}
      <div className="flex flex-wrap justify-center gap-x-6 gap-y-2 border border-border items-center text-sm text-foreground bg-background p-2 rounded-md">
        <span className="font-semibold text-primary">{title}</span>
        <span>
          📊 점수: <b>{score}</b> / {totalPossibleScore}
        </span>
        <span>
          ✅ 정답: <b>{correctAnswers}</b> / {totalQuestions}
        </span>
        <span>
          ⏳ 시간: <b>{timeTaken}</b>초
        </span>
        <span>
          🎖️ 경험치: <b> {newTotalExperience} </b>
        </span>
      </div>

      {/* ✅ 정답률 Progress Bar */}
      <div className="mt-6">
        <div className="flex justify-between items-center">
          <h2 className="text-lg font-semibold">정답률</h2>
          <p className="text-center text-sm mt-1">
            {correctPercentage.toFixed(1)}%
          </p>
        </div>
        <div className="w-full bg-gray-300 h-4 rounded-md mt-2">
          <div
            className={`h-4 rounded-md transition-all ${
              correctPercentage >= 50 ? "bg-green-500" : "bg-red-500"
            }`}
            style={{ width: `${correctPercentage}%` }}
          />
        </div>
      </div>

      {/* ✅ 질문별 결과 */}
      <div className="mt-6 space-y-4">
        <h2 className="text-lg font-semibold">📋 문제별 정답 확인</h2>
        {questions.map((q, index) => {
          const isCorrect = q.isCorrect;
          const isNoAnswer = !q.yourAnswer;
          const answerIcon = isCorrect ? "🟢" : isNoAnswer ? "⏳" : "🔴";

          return (
            <div
              key={q.id}
              className={`p-4 border rounded-lg ${
                isCorrect
                  ? "border-green-500 bg-green-100"
                  : "border-red-500 bg-red-100"
              }`}
            >
              <p className="font-semibold">
                {index + 1}. {q.questionText}
              </p>
              <p className="text-sm">
                <span className="font-bold text-green-700">
                  ✅ 정답: {q.correctAnswer}
                </span>
              </p>
              <p className="text-sm flex items-center gap-1">
                <span className="font-bold text-red-700">
                  {answerIcon} 당신의 답: {q.yourAnswer || "미응답"}
                </span>
              </p>
              <p className="text-sm text-gray-600">💡 설명: {q.explanation}</p>
            </div>
          );
        })}
      </div>

      {/* ✅ 다시 풀기 / 홈으로 이동 버튼 */}
      <div className="flex justify-center gap-4 flex-wrap mt-6">
        <Button
          variant="secondary"
          onClick={() => router.push("/quizzes")}
          className="text-black"
        >
          퀴즈 목록으로
        </Button>
        <RetryQuizButton quizId={quizId} />
      </div>
    </div>
  );
};

export default QuizResultPage;
