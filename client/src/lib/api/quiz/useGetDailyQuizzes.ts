import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { QuizResponse } from "@/lib/types/quiz";

/**
 * ✅ 일일 퀴즈 목록 조회 API
 * @returns `DailyQuiz[]` 타입의 데이터
 */
export const useGetDailyQuizzes = () => {
  return useQuery({
    queryKey: ["dailyQuizzes"],
    queryFn: async () => {
      const response = await httpClient.get<CommonApiResponse<QuizResponse>>(
        "/quizzes/daily"
      );
      return response.data;
    },
    staleTime: 1000 * 60 * 20, // 20분 동안 캐싱된 데이터 유지
  });
};
