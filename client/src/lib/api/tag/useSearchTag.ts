import { useQuery } from "@tanstack/react-query";
import { CommonApiResponse } from "@/lib/types/common";
import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

interface SearchTagsResponse {
  content: TagResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

const searchTags = async (
  name: string,
  page: number = 0,
  size: number = 10
) => {
  const params = new URLSearchParams();
  if (name) params.append("name", name);
  params.append("page", page.toString());
  params.append("size", size.toString());

  const response = await httpClient.get<CommonApiResponse<SearchTagsResponse>>(
    `/tags/search?${params.toString()}`
  );
  return response.data;
};

/**
 * @returns 태그 검색 API
 * @param name 검색어
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @returns 검색 결과
 */

export const useSearchTags = (
  name: string,
  page: number = 0,
  size: number = 10
) => {
  return useQuery<CommonApiResponse<SearchTagsResponse>, Error>({
    queryKey: ["searchTags", name, page],
    queryFn: () => searchTags(name, page, size),
    enabled: !!name, // 검색어가 있을 때만 요청 실행
  });
};
