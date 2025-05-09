import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserStatistics } from "@/lib/types/user";

// ✅ 사용자 통계 조회 API
const fetchUserStatistics = async (userId?: number) => {
  const endpoint = userId
    ? `/users/${userId}/statistics`
    : "/users/me/statistics";
  const response = await httpClient.get<CommonApiResponse<UserStatistics>>(
    endpoint
  );
  return response.data.data ?? null;
};

// ✅ 사용자 통계 조회 훅 (useQuery)
export const useUserStatistics = (userId?: number) => {
  return useQuery({
    queryKey: ["userStatistics", userId ?? null],
    queryFn: () => fetchUserStatistics(userId),
    staleTime: userId ? 0 : 1000 * 60 * 10,
  });
};
