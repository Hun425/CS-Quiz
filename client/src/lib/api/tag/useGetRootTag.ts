import { useQuery } from "@tanstack/react-query";

import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

/**
 * @returns 루트 태그 목록을 조회하는 API
 */
const getRootTags = async () => {
  const response = await httpClient.get<CommonApiResponse<TagResponse[]>>(
    `/tags/roots`
  );
  return response.data;
};

export const useGetRootTags = () => {
  return useQuery<CommonApiResponse<TagResponse[]>, Error>({
    queryKey: ["rootTags"],
    queryFn: getRootTags,
  });
};
