import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { QuizSummaryResponse } from "@/lib/types/quiz";
import { useAuthStore } from "@/store/authStore";

/**
 * ✅ 추천 퀴즈 목록 조회 훅
 * `/api/quizzes/recommended`
 * pagination 없이 추천 퀴즈 목록을 가져옴
 * @param limit 가져올 퀴즈 개수 (선택적)
 * @return `QuizSummaryResponse[]` 타입의 데이터를 반환
 */

export const useGetRecommendedQuizzes = ({ limit = 5 }: { limit?: number }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["recommendedQuizzes", limit, isAuthenticated],
    queryFn: async () => {
      if (!isAuthenticated) return { data: [] };
      const response = await httpClient.get<
        CommonApiResponse<QuizSummaryResponse[]>
      >(`/quizzes/recommended?limit=${limit}`);
      return response.data;
    },
    enabled: isAuthenticated,
    staleTime: 1000 * 60 * 30, // 30분 동안 캐싱된 데이터 유지
  });
};
