import { useEffect, useState } from "react";
import { getPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import { useQuizStore } from "@/store/quizStore";
import { QuizPlayResponse } from "@/lib/types/quiz";

interface UseLoadQuizPlayDataResult {
  quizPlayData: QuizPlayResponse | null;
  isLoading: boolean;
  error: boolean;
}

export default function useLoadQuizPlayData(
  quizId: number
): UseLoadQuizPlayDataResult {
  const [quizPlayData, setQuizPlayData] = useState<QuizPlayResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(false);
  const { setQuiz, setQuizPlayData: setGlobalQuizPlayData } = useQuizStore();

  useEffect(() => {
    const load = async () => {
      setIsLoading(true);
      setError(false);

      const now = Date.now();
      const cached = sessionStorage.getItem("lastAttempt");

      if (cached) {
        try {
          const {
            attemptId,
            quizId: cachedQuizId,
            endTime,
          } = JSON.parse(cached);
          if (attemptId && cachedQuizId === quizId && endTime > now) {
            const key = `quiz-${quizId}-${attemptId}`;
            const cachedData = sessionStorage.getItem(key);
            if (cachedData) {
              const parsed = JSON.parse(cachedData);
              setQuiz(
                quizId,
                parsed.quizAttemptId,
                parsed.timeLimit,
                parsed.questionCount,
                now,
                endTime
              );
              setGlobalQuizPlayData(parsed);
              setQuizPlayData(parsed);
              setIsLoading(false);
              return;
            }
          }
        } catch {
          console.warn("❗ 캐시 파싱 오류");
        }
      }

      // 🔄 캐시 불가 → API 요청
      try {
        const fresh = await getPlayableQuiz(quizId);
        const endTime = now + fresh.timeLimit * fresh.questionCount * 1000;

        setQuiz(
          quizId,
          fresh.quizAttemptId,
          fresh.timeLimit,
          fresh.questionCount,
          now,
          endTime
        );
        setGlobalQuizPlayData(fresh);
        setQuizPlayData(fresh);

        sessionStorage.setItem(
          "lastAttempt",
          JSON.stringify({ attemptId: fresh.quizAttemptId, quizId, endTime })
        );
        sessionStorage.setItem(
          `quiz-${quizId}-${fresh.quizAttemptId}`,
          JSON.stringify(fresh)
        );
      } catch {
        setError(true);
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, [quizId, setQuiz, setGlobalQuizPlayData]);

  return { quizPlayData, isLoading, error };
}
