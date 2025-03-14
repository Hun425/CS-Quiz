import { useQuery } from "@tanstack/react-query";
import axios from "axios";

interface QuizStatistics {
  totalPlays: number;
  averageScore: number;
  // 추가 통계 필드 필요 시 확장
}

export const useGetQuizStatistics = (quizId: number) => {
  return useQuery<QuizStatistics, Error>({
    queryKey: ["quizStatistics", quizId],
    queryFn: () =>
      axios.get(`/api/quizzes/${quizId}/statistics`).then((res) => res.data),
    enabled: !!quizId,
  });
};
