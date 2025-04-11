import { useEffect, useState } from "react";
import { getPlayableQuiz } from "@/lib/api/quiz/useGetPlayableQuiz";
import { useQuizStore } from "@/store/quizStore";
import { QuizPlayResponse } from "@/lib/types/quiz";

interface UseLoadQuizPlayDataResult {
  quizPlayData: QuizPlayResponse | null;
  error: boolean;
  isLoading: boolean;
}

// ✅ 호출 전용 로직만 담당 (reset 등은 외부에서 관리)
export default function useLoadQuizPlayData(
  quizId: number
): UseLoadQuizPlayDataResult {
  const [quizPlayData, setQuizPlayData] = useState<QuizPlayResponse | null>(
    null
  );
  const [error, setError] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const { setQuiz, setQuizPlayData: setGlobalQuizPlayData } = useQuizStore();

  console.log(typeof quizId, quizId, "quizId");
  useEffect(() => {
    const loadQuiz = async () => {
      console.log("🔄 [useLoadQuizPlayData] loadQuiz 실행됨");

      const lastAttemptRaw = sessionStorage.getItem("lastAttempt");

      console.log("ℹ️ 세션 캐시 확인", lastAttemptRaw);

      if (lastAttemptRaw) {
        try {
          const {
            attemptId: cachedAttemptId,
            quizId: cachedQuizId,
            endTime,
          } = JSON.parse(lastAttemptRaw);

          const now = Date.now();
          console.log("🧾 lastAttempt found", {
            cachedAttemptId,
            cachedQuizId,
            endTime,
            now,
          });

          if (
            cachedAttemptId &&
            cachedQuizId === quizId &&
            endTime &&
            now < endTime
          ) {
            const key = `quiz-${quizId}-${cachedAttemptId}`;
            const cachedQuiz = sessionStorage.getItem(key);
            if (cachedQuiz) {
              const parsed = JSON.parse(cachedQuiz);
              console.log("✅ 세션에서 퀴즈 복원 성공", parsed);

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
            } else {
              console.log("❌ 세션에 퀴즈 캐시가 없음");
            }
          } else {
            console.log("⏱ 세션 만료 또는 다른 퀴즈 ID");
          }
        } catch (err) {
          console.warn("⚠️ 세션 캐시 파싱 오류", err);
        }
      } else {
        console.log("ℹ️ 세션 없음 → 새 퀴즈 요청 예정");
      }

      // 🔄 캐시 복원 실패 → 새 요청
      try {
        console.log("📡 API 요청: getPlayableQuiz");
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

        const key = `quiz-${quizId}-${data.quizAttemptId}`;
        sessionStorage.setItem(
          "lastAttempt",
          JSON.stringify({
            attemptId: data.quizAttemptId,
            quizId,
            endTime,
          })
        );
        sessionStorage.setItem(key, JSON.stringify(data));
        console.log("✅ API 퀴즈 요청 후 세션 저장 완료");
      } catch (err) {
        console.error("❌ 퀴즈 API 요청 실패", err);
        setError(true);
      } finally {
        setIsLoading(false);
      }
    };

    loadQuiz();

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [quizId]);

  return { quizPlayData, error, isLoading };
}
