import { useQuery } from "@tanstack/react-query";
import { QuizDetailResponse } from "@/lib/types/quiz";
import httpClient from "../httpClient";

/**
 * ✅ 퀴즈 상세 정보 조회 API
 * @param quizId - 조회할 퀴즈 ID
 * @returns `QuizDetailResponse` 타입의 데이터
 */
export const useGetQuizDetail = (quizId: number) => {
  return useQuery({
    queryKey: ["quizDetail", quizId],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<QuizDetailResponse>
      >(`/quizzes/${quizId}`);
      return response.data.data;
    },
    enabled: !!quizId,
    staleTime: 1000 * 60 * 5,
  });
};
