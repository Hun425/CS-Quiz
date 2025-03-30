import { useEffect, useState } from "react";
import { getPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import { useQuizStore } from "@/store/quizStore";
import { QuizPlayResponse } from "@/lib/types/quiz";

interface UseLoadQuizPlayDataResult {
  quizPlayData: QuizPlayResponse | null;
  error: boolean;
  isLoading: boolean;
}

export default function useLoadQuizPlayData(
  quizId: number
): UseLoadQuizPlayDataResult {
  const [quizPlayData, setQuizPlayData] = useState<QuizPlayResponse | null>(
    null
  );
  const [error, setError] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const { setQuiz, setQuizPlayData: setGlobalQuizPlayData } = useQuizStore();

  useEffect(() => {
    const loadQuiz = async () => {
      const lastAttemptRaw = sessionStorage.getItem("lastAttempt");

      if (lastAttemptRaw) {
        try {
          const { attemptId: cachedAttemptId, endTime } =
            JSON.parse(lastAttemptRaw);
          const now = Date.now();

          if (cachedAttemptId && endTime && now < endTime) {
            const cachedQuiz = sessionStorage.getItem(
              `quiz-${cachedAttemptId}`
            );
            if (cachedQuiz) {
              const parsed = JSON.parse(cachedQuiz);

              setQuizPlayData(parsed);
              setGlobalQuizPlayData(parsed);
              setQuiz(
                quizId,
                parsed.quizAttemptId,
                parsed.timeLimit,
                parsed.questionCount,
                now,
                endTime
              );
              setIsLoading(false);
              return;
            }
          }
        } catch {
          console.warn("세션 캐시 파싱 오류");
        }
      }

      // 🔄 캐시 복원 실패 or 만료 → 새 요청
      try {
        const data = await getPlayableQuiz(quizId);
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
        setGlobalQuizPlayData(data);

        sessionStorage.setItem(
          "lastAttempt",
          JSON.stringify({
            attemptId: data.quizAttemptId,
            endTime,
          })
        );
        sessionStorage.setItem(
          `quiz-${data.quizAttemptId}`,
          JSON.stringify(data)
        );
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
      } catch (err) {
        setError(true);
      } finally {
        setIsLoading(false);
      }
    };

    loadQuiz();
  }, [quizId, setQuiz, setGlobalQuizPlayData]);

  return { quizPlayData, error, isLoading };
}
