import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { QuizSummaryResponse } from "@/lib/types/quiz";
import httpClient from "@/lib/api/httpClient";

interface SearchParams {
  title?: string;
  difficultyLevel?: string;
  quizType?: string;
  tagIds?: number[];
  minQuestions?: number;
  maxQuestions?: number;
  orderBy?: string;
}

/**
 * ✅ 퀴즈 검색 API 요청 함수
 * - `searchParams` + `pageable`(page, size, sort) 추가
 */
const searchQuizzes = async (
  searchParams: SearchParams,
  page: number,
  size: number
): Promise<PageResponse<QuizSummaryResponse>> => {
  // ✅ Query Params 변환
  const params = new URLSearchParams();

  if (searchParams.title) params.append("title", searchParams.title);
  if (searchParams.difficultyLevel)
    params.append("difficultyLevel", searchParams.difficultyLevel);
  if (searchParams.quizType) params.append("quizType", searchParams.quizType);
  if (searchParams.minQuestions)
    params.append("minQuestions", searchParams.minQuestions.toString());
  if (searchParams.maxQuestions)
    params.append("maxQuestions", searchParams.maxQuestions.toString());
  if (searchParams.orderBy) params.append("orderBy", searchParams.orderBy);

  // ✅ `tagIds` 배열을 쉼표로 연결하여 전달 (배열 지원X)
  if (searchParams.tagIds && searchParams.tagIds.length > 0) {
    params.append("tagIds", searchParams.tagIds.join(","));
  }

  // ✅ 페이징 처리
  params.append("page", page.toString());
  params.append("size", size.toString());

  // ✅ API 요청
  const response = await httpClient.get<
    CommonApiResponse<PageResponse<QuizSummaryResponse>>
  >(`/quizzes/search?${params.toString()}`);

  return response.data.data;
};

/**
 * ✅ React Query로 퀴즈 검색
 */
export const useSearchQuizzes = (
  searchParams: SearchParams = {},
  page: number = 0,
  size: number = 10
) => {
  return useQuery({
    queryKey: [
      "searchQuizzes",
      searchParams.title,
      searchParams.difficultyLevel,
      searchParams.quizType,
      searchParams.tagIds,
      page,
      size,
    ],
    queryFn: () => searchQuizzes(searchParams, page, size),
    placeholderData: keepPreviousData,
    enabled: true,
    staleTime: 0,
  });
};
