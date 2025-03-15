import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { QuizSummaryResponse } from "@/lib/types/quiz";

/**
 * ✅ 추천 퀴즈 목록 조회 훅
 * `/api/quizzes/recommended`
 * pagination 없이 추천 퀴즈 목록을 가져옴
 * @return`QuizSummaryResponse[]` 타입의 데이터를 반환
 */

export const useGetRecommendedQuizzes = () => {
  return useQuery({
    queryKey: ["recommendedQuizzes"],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<QuizSummaryResponse[]>
      >("/quizzes/recommended");
      return response.data;
    },
  });
};
