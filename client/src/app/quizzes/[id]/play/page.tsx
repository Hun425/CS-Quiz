"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { getPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import { useQuizStore } from "@/store/quizStore";
import QuizPlayClientPage from "./QuizPlayClientPage";

export default function QuizPlayPage() {
  const { id } = useParams();
  const quizId = Number(id);
  const router = useRouter();

  const {
    quizId: storedQuizId,
    attemptId,
    quizPlayData,
    setQuiz,
    setQuizPlayData,
    resetQuiz,
  } = useQuizStore();

  const [error, setError] = useState(false);

  useEffect(() => {
    // ✅ 캐시된 데이터가 없을 경우에만
    if (!quizPlayData) {
      // 🔑 key 만들기
      const key =
        storedQuizId && attemptId ? `quiz-${storedQuizId}-${attemptId}` : null;

      if (key) {
        const cached = sessionStorage.getItem(key);
        if (cached) {
          setQuizPlayData(JSON.parse(cached));
          return;
        }
      }

      // ✅ 새 API 요청
      getPlayableQuiz(quizId)
        .then((data) => {
          const now = Date.now();
          const endTime = now + data.timeLimit * 1000;

          setQuiz(
            quizId,
            data.quizAttemptId,
            data.timeLimit,
            data.questionCount,
            now,
            endTime
          );
          setQuizPlayData(data);

          // 💾 캐시 저장
          const newKey = `quiz-${quizId}-${data.quizAttemptId}`;
          sessionStorage.setItem(newKey, JSON.stringify(data));
        })
        .catch(() => setError(true));
    }
  }, [quizId, attemptId, storedQuizId, quizPlayData, setQuiz, setQuizPlayData]);

  useEffect(() => {
    // ✅ URL에 쿼리 붙여서 히스토리에 강제로 기록
    window.history.pushState(
      { page: "quiz-play" },
      "",
      window.location.href + "?playing=true"
    );

    const handlePopState = () => {
      console.log("뒤로가기 감지됨!");
      resetQuiz();
      router.replace("/quizzes");
    };

    window.addEventListener("popstate", handlePopState);
    return () => {
      window.removeEventListener("popstate", handlePopState);
    };
  }, [resetQuiz, router]);

  if (error)
    return (
      <div className="text-red-500 text-center py-12 min-h-screen">
        ❌ 퀴즈 로딩 실패
      </div>
    );

  if (!quizPlayData)
    return (
      <div className="text-center py-12 min-h-screen flex items-center justify-center">
        🔄 퀴즈 데이터를 불러오는 중...
      </div>
    );

  return <QuizPlayClientPage initialData={quizPlayData} quizId={quizId} />;
}
