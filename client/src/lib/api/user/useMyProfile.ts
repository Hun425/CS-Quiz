import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserProfile } from "@/lib/types/user";
import { useProfileStore } from "@/store/profileStore";
import { useToastStore } from "@/store/toastStore";

// ✅ 내 프로필 조회 API
const fetchMyProfile = async () => {
  console.log("내 프로필 조회");
  const response = await httpClient.get<{
    success: boolean;
    data: UserProfile;
  }>("/users/me/profile");

  return response.data;
};

export const useMyProfile = () => {
  const { showToast } = useToastStore.getState();

  return useQuery({
    queryKey: ["myProfile"],
    queryFn: async () => {
      const storedUser = useProfileStore.getState().userProfile;
      if (storedUser) return storedUser;

      const response = await fetchMyProfile();
      if (response.success) {
        useProfileStore.getState().setUserProfile(response.data);
        showToast("프로필 조회 성공", "success");
        return response.data;
      } else {
        showToast("프로필 조회 실패", "warning");
        throw new Error("프로필 조회 실패");
      }
    },
    staleTime: 1000 * 60 * 10,
    refetchOnWindowFocus: true,
  });
};
