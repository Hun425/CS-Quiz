import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { RecentActivity } from "@/lib/types/user";

// ✅ 최근 활동 조회 API
const fetchRecentActivities = async (userId?: number, limit: number = 10) => {
  const endpoint = userId
    ? `/users/${userId}/recent-activities?limit=${limit}`
    : `/users/me/recent-activities?limit=${limit}`;
  const response = await httpClient.get<CommonApiResponse<RecentActivity[]>>(
    endpoint
  );

  return response.data.data;
};

// ✅ 최근 활동 조회 훅 (useQuery)
export const useUserRecentActivities = (
  userId?: number,
  limit: number = 10
) => {
  return useQuery({
    queryKey: ["recentActivities", userId, limit],
    queryFn: () => fetchRecentActivities(userId, limit),
    staleTime: userId ? 0 : 1000 * 60 * 10,
  });
};
