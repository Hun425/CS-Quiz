import { useMutation, useQueryClient } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { QuizCreateRequest, QuizPlayResponse } from "@/lib/types/quiz";

/**
 * ✅ 퀴즈 생성 API
 * - 새로운 퀴즈를 생성합니다.
 * @returns `QuizResponse` 타입의 데이터
 * @param data QuizCreateRequest - 생성할 퀴즈 정보
 */

export const useCreateQuiz = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: QuizCreateRequest) => {
      const response = await httpClient.post<
        CommonApiResponse<QuizPlayResponse>
      >("/quizzes", data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["quizzes"] });
    },
  });
};
