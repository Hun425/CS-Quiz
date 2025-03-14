import { useMutation, useQueryClient } from "@tanstack/react-query";
import axios from "axios";

interface QuizCreateData {
  title: string;
  description?: string;
  // 추가 생성 데이터 필드 필요 시 확장
}

export const usePostQuiz = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: QuizCreateData) =>
      axios.post("/api/quizzes", data).then((res) => res.data),
    onSuccess: () => {
      queryClient.invalidateQueries(["quizzes"]); // 캐시 갱신
    },
  });
};
