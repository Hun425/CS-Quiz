import { useQuery } from "@tanstack/react-query";
import { CommonApiResponse } from "@/lib/types/common";
import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

/**
 * @returns 태그 전체 목록을 조회하는 API

 */

const getAllTags = async () => {
  const response = await httpClient.get<CommonApiResponse<TagResponse[]>>(
    `/tags`
  );
  return response.data;
};

export const useGetAllTags = () => {
  return useQuery<CommonApiResponse<TagResponse[]>, Error>({
    queryKey: ["allTags"],
    queryFn: getAllTags,
    staleTime: 1000 * 60 * 60 * 24, // ✅ 24시간 이후 만료
  });
};
