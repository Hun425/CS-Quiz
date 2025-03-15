import { useQuery } from "@tanstack/react-query";
import httpClient from "../httpClient";
import { PageResponse } from "@/lib/types/common";
import { QuizSummaryResponse } from "@/lib/types/quiz";
import { CommonApiResponse } from "@/lib/types/common";

/**
 * ✅ 특정 태그에 해당하는 퀴즈 목록 조회 훅 (페이징 지원)
 * - `/api/quizzes/tags/{tagId}` API 호출
 * - `pageable`(page, size, sort) 파라미터 추가
 * @param tagId 조회할 태그 ID
 * @param page 페이지 번호 (기본값: 0)
 * @param size 페이지 크기 (기본값: 10)
 * @param sort 정렬 기준 (기본값: "createdAt")
 * @returns 특정 태그에 해당하는 퀴즈 리스트 (페이징 지원)
 */
export const useGetTagedQuiz = (
  tagId: number,
  page: number = 0,
  size: number = 10,
  sort: string = "createdAt"
) => {
  return useQuery<CommonApiResponse<PageResponse<QuizSummaryResponse>>, Error>({
    queryKey: ["quizTags", tagId, page, size, sort],
    queryFn: async () => {
      const response = await httpClient.get<
        CommonApiResponse<PageResponse<QuizSummaryResponse>>
      >(`/quizzes/tags/${tagId}`, {
        params: { page, size, sort },
      });

      return response.data;
    },
    enabled: !!tagId, // ✅ tagId가 존재할 때만 실행
    staleTime: 1000 * 60 * 5, // 5분 동안 캐싱 유지
  });
};
