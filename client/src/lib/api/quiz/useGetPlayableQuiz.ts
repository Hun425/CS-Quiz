import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { CommonApiResponse } from "@/lib/types/common";
import { QuizResponse } from "@/lib/types/quiz";

/**
 *  퀴즈 플레이 가능 여부 조회 API
 * @param quizId
 * @returns `QuizResponse` 타입의 데이터
 */
const getPlayableQuiz = async (quizId: number) => {
  const response = await httpClient.get<CommonApiResponse<QuizResponse>>(
    `/quizzes/${quizId}/play`
  );
  console.log(quizId);
  return response.data.data;
};

export const useGetPlayableQuiz = (quizId: number) => {
  return useQuery({
    queryKey: ["playableQuiz", quizId],
    queryFn: () => getPlayableQuiz(quizId),
    enabled: !!quizId, // quizId가 있을 때만 실행
    staleTime: 1000 * 60 * 5,
  });
};
