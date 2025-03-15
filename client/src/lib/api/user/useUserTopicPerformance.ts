import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { TopicPerformance } from "@/lib/types/user";

// ✅ 주제별 성과 조회 API
const fetchTopicPerformance = async (userId?: number) => {
  const endpoint = userId
    ? `/users/${userId}/topic-performance`
    : "/users/me/topic-performance";
  const response = await httpClient.get<CommonApiResponse<TopicPerformance[]>>(
    endpoint
  );

  return response.data.data;
};

// ✅ 주제별 성과 조회 훅 (useQuery)
export const useUserTopicPerformance = (userId?: number) => {
  return useQuery({
    queryKey: ["topicPerformance", userId],
    queryFn: () => fetchTopicPerformance(userId),
    staleTime: userId ? 0 : 1000 * 60 * 10, // ✅ 내 데이터는 10분 캐싱, 남의 데이터는 매번 새로 패칭
    enabled: true,
  });
};
