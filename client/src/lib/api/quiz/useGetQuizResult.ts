import httpClient from "../httpClient";
import { useQuery } from "@tanstack/react-query";
import { QuizResultResponse } from "@/lib/types/quiz";

/**
 * ✅ 퀴즈 결과 조회 API (getQuizResult)
 * - 특정 퀴즈 응시(`quizAttemptId`)의 결과를 조회
 * - `quizid`: 퀴즈 ID
 * - `quizAttemptId`: 퀴즈 응시 ID
 * - 응답 타입: `CommonApiResponse<QuizResultResponse>`
 */
const getQuizResult = async (quizAttemptId: number, quizid: number) => {
  const response = await httpClient.get<CommonApiResponse<QuizResultResponse>>(
    `/quizzes/${quizid}/results/${quizAttemptId}`
  );

  return response.data.data;
};

/**
 * ✅ 퀴즈 결과 조회 훅 (useGetQuizResult)
 * - `useQuery`를 사용하여 퀴즈 응시 결과를 가져오는 React Query 훅
 * - `quizAttemptId`: 퀴즈 응시 ID
 * - `quizid`: 퀴즈 ID
 * - `queryKey`: ["quizResult", quizAttemptId, quizid]
 * - `staleTime`: 0 (항상 최신 데이터를 가져오기 위해 캐싱 없이 즉시 refetch)
 */
export default function useGetQuizResult(
  quizAttemptId: number,
  quizid: number
) {
  return useQuery({
    queryKey: ["quizResult", quizAttemptId, quizid],
    queryFn: () => getQuizResult(quizAttemptId, quizid),
    staleTime: 0,
  });
}
