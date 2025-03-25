import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { CommonApiResponse } from "@/lib/types/common";
import { QuizStatisticsResponse } from "@/lib/types/quiz";

/**
 * ✅ 특정 퀴즈의 통계 정보를 가져오는 훅
 * - `quizId`가 있어야 실행됨 (`enabled: !!quizId`)
 * - 퀴즈의 시도 횟수, 평균 점수, 완료율 등의 데이터를 반환
 */
export const useGetQuizStatistics = (quizId: number) => {
  return useQuery({
    queryKey: ["quizStatistics", quizId],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<QuizStatisticsResponse>
      >(`/quizzes/${quizId}/statistics`);
      return response.data.data;
    },
    enabled: !!quizId, // quizId가 존재할 때만 실행
    staleTime: 1000 * 60 * 10, // 10분 동안 캐싱된 데이터 유지
  });
};
