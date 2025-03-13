import { useQuery } from "@tanstack/react-query";
import { TagResponse } from "../../types/tag";
import httpClient from "../httpClient";
import { CommonApiResponse } from "../../types/common";

const fetchTags = async (): Promise<CommonApiResponse<TagResponse[]>> => {
  const { data } = await httpClient.get<CommonApiResponse<TagResponse[]>>(
    "/tags"
  );

  return data;
};

export const useGetTags = () => {
  return useQuery({
    queryKey: ["tags"],
    queryFn: fetchTags,
    staleTime: 1000 * 60 * 5,
  });
};
