import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { QuizPlayResponse } from "@/lib/types/quiz";
import { useAuthStore } from "@/store/authStore";

/**
 * ✅ 일일 퀴즈 목록 조회 API
 * @returns `DailyQuizResponse` 타입의 데이터
 */
export const useGetDailyQuizzes = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return useQuery({
    queryKey: ["dailyQuizzes"],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<QuizPlayResponse>
      >("/quizzes/daily");
      return response.data;
    },
    staleTime: 1000 * 60 * 30, // 30분 동안 캐싱된 데이터 유지
    enabled: isAuthenticated,
  });
};
