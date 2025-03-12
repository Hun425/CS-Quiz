import { useQuery } from "@tanstack/react-query";
import { TagResponse } from "../types/tag";
import httpClient from "./httpClient";
import { CommonApiResponse } from "../types/common";
import { toast } from "react-toastify"; // 토스트 라이브러리

const fetchTags = async (): Promise<CommonApiResponse<TagResponse[]>> => {
  const { data } = await httpClient.get<CommonApiResponse<TagResponse[]>>(
    "/tags"
  );

  return data; // 항상 CommonApiResponse<TagResponse[]> 반환
};

export const useGetTags = () => {
  return useQuery({
    queryKey: ["tags"],
    queryFn: fetchTags,
    staleTime: 1000 * 60 * 5,
    onSuccess: (data) => {
      if (!data.success) {
        toast.error(`태그 불러오기 실패: ${data.message}`);
      }
    },
  });
};
