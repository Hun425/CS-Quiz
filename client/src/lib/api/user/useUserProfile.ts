import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserProfile } from "@/lib/types/user";

// ✅ 특정 사용자 프로필 조회 API
const fetchUserProfile = async (userId: number) => {
  const response = await httpClient.get<{
    success: boolean;
    data: UserProfile;
  }>(`/users/${userId}/profile`);
  return response.data;
};

// ✅ 특정 사용자 프로필 조회 훅 (캐싱 O)
export const useUserProfile = (userId: number) => {
  return useQuery({
    queryKey: ["userProfile", userId ?? null],
    queryFn: () => fetchUserProfile(userId),
    staleTime: 0,
    refetchOnWindowFocus: true,
  });
};
