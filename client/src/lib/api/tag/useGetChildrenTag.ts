import { useQuery } from "@tanstack/react-query";
import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

/**
 * @returns 태그의 자식 태그 목록을 조회하는 API
 * @param parentTagId 부모 태그 ID
 */

const getChildTags = async (parentTagId: number) => {
  const response = await httpClient.get<CommonApiResponse<TagResponse[]>>(
    `/tags/${parentTagId}/children`
  );
  return response.data;
};

export const useGetChildTags = (parentTagId: number) => {
  return useQuery<CommonApiResponse<TagResponse[]>, Error>({
    queryKey: ["childTags", parentTagId],
    queryFn: () => getChildTags(parentTagId),
    enabled: !!parentTagId, // ✅ parentTagId가 있을 때만 실행
  });
};
