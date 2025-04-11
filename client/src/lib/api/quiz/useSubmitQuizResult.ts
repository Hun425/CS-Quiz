import { useMutation, useQueryClient } from "@tanstack/react-query";
import httpClient from "../httpClient";

import { QuizSubmitRequest, QuizResultResponse } from "@/lib/types/quiz";

/**
 * ✅ 퀴즈 제출 훅
 * @description 사용자가 퀴즈를 완료한 후 제출하는 API를 호출합니다.
 * @permission 로그인한 사용자만 이용 가능
 * @param {number} quizId - 제출할 퀴즈의 ID
 * @param {QuizSubmitRequest} submitData - 제출할 답변 데이터
 * @returns {MutationResult<CommonApiResponse<QuizResultResponse>>} 퀴즈 결과 응답
 */
const submitQuiz = async (quizId: number, submitData: QuizSubmitRequest) => {
  const response = await httpClient.post<CommonApiResponse<QuizResultResponse>>(
    `/quizzes/${quizId}/results`,
    submitData
  );

  console.log(submitData, "submitData");
  return response.data;
};

/**
 * ✅ 퀴즈 제출을 위한 React Query Mutation 훅
 */
export const useSubmitQuiz = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      quizId,
      submitData,
    }: {
      quizId: number;
      submitData: QuizSubmitRequest;
    }) => submitQuiz(quizId, submitData),
    onSuccess: (data, { quizId }) => {
      console.log("🎉 퀴즈 제출 성공:", data);

      // ✅ 퀴즈 결과 페이지에 대한 캐시 무효화
      queryClient.invalidateQueries({ queryKey: ["quizResult", quizId] });
    },
    onError: (error) => {
      console.error("❌ 퀴즈 제출 실패:", error);
    },
  });
};
