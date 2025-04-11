import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { QuizPlayResponse } from "@/lib/types/quiz";
import { useAuthStore } from "@/store/authStore";
import { useQuizStore } from "@/store/quizStore";

/**
 * ✅ 퀴즈 플레이 가능 여부 조회 API
 * @param quizId 퀴즈 ID
 * @returns `QuizPlayResponse` 타입의 데이터
 */
export const getPlayableQuiz = async (quizId: number) => {
  const response = await httpClient.get<CommonApiResponse<QuizPlayResponse>>(
    `/quizzes/${quizId}/play`
  );
  console.log(response.data.data);
  return response.data.data;
};

export const useGetPlayableQuiz = (quizId: number, shouldFetch: boolean) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const { attemptId, quizId: storedQuizId } = useQuizStore();

  // ✅ 기존 attemptId가 있다면 API 호출 안 함
  const shouldFetchQuiz =
    shouldFetch && !(attemptId && storedQuizId === quizId);

  return useQuery({
    queryKey: ["playableQuiz", quizId],
    queryFn: () => getPlayableQuiz(quizId),
    enabled: !!quizId && isAuthenticated && shouldFetchQuiz,
    staleTime: 1000 * 60 * 20, // 20분 캐싱
  });
};
