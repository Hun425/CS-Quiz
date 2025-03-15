import httpClient from "../httpClient";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { QuizResponse, QuizCreateRequest } from "@/lib/types/quiz";

/**
 * ✅ 퀴즈 업데이트 API
 * - 특정 퀴즈를 업데이트하고, 성공하면 해당 퀴즈 캐시를 무효화하여 최신 데이터로 갱신
 */
export const useUpdateQuiz = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      quizId,
      data,
    }: {
      quizId: number;
      data: QuizCreateRequest;
    }) => {
      const response = await httpClient.put<CommonApiResponse<QuizResponse>>(
        `/quizzes/${quizId}`,
        data
      );
      return response.data;
    },
    onSuccess: (_, { quizId }) => {
      queryClient.invalidateQueries({ queryKey: ["quizDetail", quizId] }); // ✅ 개별 퀴즈 데이터 무효화
      queryClient.invalidateQueries({ queryKey: ["quizzes"] }); // ✅ 전체 퀴즈 목록 캐시 무효화
    },
  });
};
