import { useQuery } from "@tanstack/react-query";

import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

/**
 * @returns 인기 태그 목록을 조회하는 API
 */
const getPopularTags = async () => {
  const response = await httpClient.get<CommonApiResponse<TagResponse[]>>(
    `/tags/popular`
  );
  return response.data;
};

export const useGetPopularTags = () => {
  return useQuery<CommonApiResponse<TagResponse[]>, Error>({
    queryKey: ["popularTags"],
    queryFn: getPopularTags,
  });
};
