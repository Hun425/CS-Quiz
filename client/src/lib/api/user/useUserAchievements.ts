import httpClient from "../httpClient";
import { useQuery } from "@tanstack/react-query";
import { Achievement } from "@/lib/types/user";

// ✅ 사용자 업적 조회 API
const fetchAchievements = async (userId?: number) => {
  const endpoint = userId
    ? `/users/${userId}/achievements`
    : "/users/me/achievements";
  const response = await httpClient.get<{
    success: boolean;
    data: Achievement[];
  }>(endpoint);
  return response.data.data;
};

// ✅ 사용자 업적 조회 훅 (useQuery)
export const useUserAchievements = (userId?: number) => {
  return useQuery({
    queryKey: ["achievements", userId],
    queryFn: () => fetchAchievements(userId),
    staleTime: userId ? 0 : 1000 * 60 * 10,
    refetchOnWindowFocus: true,
  });
};
