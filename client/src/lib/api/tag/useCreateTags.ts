import { useMutation } from "@tanstack/react-query";
import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

interface CreateTagRequest {
  name: string;
  description: string;
  parentId: number | null;
  synonyms: string[];
}

const createTag = async (data: CreateTagRequest) => {
  const response = await httpClient.post<CommonApiResponse<TagResponse>>(
    "/tags",
    data
  );
  return response.data;
};

export const useCreateTag = () => {
  return useMutation<CommonApiResponse<TagResponse>, Error, CreateTagRequest>({
    mutationFn: createTag,
    onSuccess: (data) => {
      console.log("태그 생성 성공:", data);
    },
    onError: (error) => {
      console.error("태그 생성 실패:", error);
    },
  });
};
