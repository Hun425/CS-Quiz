import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserProfile } from "@/lib/types/user";
import { useProfileStore } from "@/store/profileStore";
import { useToastStore } from "@/store/toastStore";

// ✅ 프로필 조회 API (내 정보 or 특정 사용자)
const fetchUserProfile = async (userId?: number): Promise<UserProfile> => {
  const endpoint = userId ? `/users/${userId}/profile` : "/users/me/profile";

  const response = await httpClient.get<CommonApiResponse<UserProfile>>(
    endpoint
  );
  return response.data.data;
};

// ✅ 프로필 조회 훅 (userId가 없으면 "내 정보")
export const useGetMyProfile = (userId?: number) => {
  const { showToast } = useToastStore.getState();
  const isMe = !userId;

  return useQuery({
    queryKey: ["userProfile", userId ?? "me"],
    queryFn: async () => {
      // 내 정보일 경우엔 store 캐시 우선 활용
      if (isMe) {
        const stored = useProfileStore.getState().userProfile;
        if (stored) return stored;
      }

      const profile = await fetchUserProfile(userId);
      if (isMe) useProfileStore.getState().setUserProfile(profile);

      showToast("프로필 조회 성공", "success");
      return profile;
    },
    staleTime: isMe ? 1000 * 60 * 10 : 0, // 내 정보만 staleTime 적용
    refetchOnWindowFocus: true,
  });
};
