import { useMutation, useQueryClient } from "@tanstack/react-query";
import axios from "axios";

interface QuizUpdateData {
  title?: string;
  description?: string;
  // 추가 업데이트 데이터 필드 필요 시 확장
}

export const useUpdateQuiz = (quizId: number) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: QuizUpdateData) =>
      axios.put(`/api/quizzes/${quizId}`, data).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries(["quizDetail", quizId]); // 캐시 갱신
    },
  });
};
