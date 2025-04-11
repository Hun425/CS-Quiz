import { useQuery } from "@tanstack/react-query";
import { TagResponse } from "@/lib/types/tag";
import httpClient from "../httpClient";

/**
 * @returns 태그 전체 목록을 조회하는 API
 */

const getAllTags = async () => {
  const response = await httpClient.get<CommonApiResponse<TagResponse[]>>(
    `/tags`
  );
  console.log("✅ 전체 태그 조회", response.data);
  return response.data;
};

export const useGetAllTags = () => {
  return useQuery<CommonApiResponse<TagResponse[]>, Error>({
    queryKey: ["allTags"],
    queryFn: getAllTags,
    refetchOnWindowFocus: true,
  });
};
