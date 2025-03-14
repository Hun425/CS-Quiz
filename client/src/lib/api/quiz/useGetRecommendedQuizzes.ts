import { useQuery } from "@tanstack/react-query";
import axios from "axios";

interface RecommendedQuiz {
  id: number;
  title: string;
  recommendationScore: number;
  // 추천 퀴즈에 필요한 필드 추가
}

export const useGetRecommendedQuizzes = () => {
  return useQuery<RecommendedQuiz[], Error>({
    queryKey: ["recommendedQuizzes"],
    queryFn: () =>
      axios.get("/api/quizzes/recommended").then((res) => res.data),
  });
};
