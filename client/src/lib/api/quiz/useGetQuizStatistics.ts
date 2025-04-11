import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";

import { QuizStatisticsResponse } from "@/lib/types/quiz";
import { useAuthStore } from "@/store/authStore";

/**
 * ✅ 특정 퀴즈의 통계 정보를 가져오는 훅
 * - 로그인한 상태에서만 실행됨 (`enabled: isAuthenticated && !!quizId`)
 * - 퀴즈의 시도 횟수, 평균 점수, 완료율 등의 데이터를 반환
 */
export const useGetQuizStatistics = (quizId: number) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["quizStatistics", quizId],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<QuizStatisticsResponse>
      >(`/quizzes/${quizId}/statistics`);
      return response.data.data;
    },
    enabled: isAuthenticated && !!quizId,
    staleTime: 1000 * 60 * 10,
  });
};
