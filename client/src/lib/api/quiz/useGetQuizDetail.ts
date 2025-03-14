import { useQuery } from "@tanstack/react-query";
import { QuizDetailResponse } from "@/lib/types/quiz";
import axios from "axios";

export const useGetQuizDetail = (quizId: number) => {
  return useQuery<QuizDetailResponse, Error>({
    queryKey: ["quizDetail", quizId],
    queryFn: () => axios.get(`/api/quizzes/${quizId}`).then((res) => res.data),
    enabled: !!quizId,
  });
};
