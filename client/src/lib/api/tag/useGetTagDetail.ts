import { useQuery } from "@tanstack/react-query";

import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

/**
 * @returns 태그 상세 조회 API
 */
const getTagDetail = async (tagId: number) => {
  const response = await httpClient.get<CommonApiResponse<TagResponse>>(
    `/tags/${tagId}`
  );
  return response.data;
};

export const useGetTagDetail = (tagId: number) => {
  return useQuery<CommonApiResponse<TagResponse>, Error>({
    queryKey: ["tagDetail", tagId],
    queryFn: () => getTagDetail(tagId),
    enabled: !!tagId,
  });
};
