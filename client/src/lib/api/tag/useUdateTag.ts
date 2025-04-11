import { useMutation } from "@tanstack/react-query";

import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

interface UpdateTagRequest {
  name: string;
  description: string;
  parentId?: number;
  synonyms: string[];
}

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
    onSuccess: (data) => {
      console.log("태그 수정 성공:", data);
    },
    onError: (error) => {
      console.error("태그 수정 실패:", error);
    },
  });
};
