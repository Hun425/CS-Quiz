import { useMutation } from "@tanstack/react-query";
import { TagResponse, UpdateTagRequest } from "@/lib/types/tag";
import httpClient from "../httpClient";

const updateTag = async ({
  tagId,
  data,
}: {
  tagId: number;
  data: UpdateTagRequest;
}) => {
  const response = await httpClient.put<CommonApiResponse<TagResponse>>(
    `/tags/${tagId}`,
    data
  );
  return response.data;
};

/**
 * @returns 태그 수정 API
 * @param tagId 태그 ID
 * @param data 수정할 데이터
 * @returns 수정된 태그 정보
 */

export const useUpdateTag = () => {
  return useMutation<
    CommonApiResponse<TagResponse>,
    Error,
    { tagId: number; data: UpdateTagRequest }
  >({
    mutationFn: updateTag,
  });
};
