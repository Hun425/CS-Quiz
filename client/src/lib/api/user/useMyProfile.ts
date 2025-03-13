import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserProfile } from "@/lib/types/user";
import { useProfileStore } from "@/store/profileStore";
import { useToastStore } from "@/store/toastStore";

// ✅ 내 프로필 조회 API
const fetchMyProfile = async () => {
  const response = await httpClient.get<{
    success: boolean;
    data: UserProfile;
  }>("/users/me/profile");
  return response.data;
};

// ✅ 내 프로필 조회 훅 (localStorage 활용)
export const useMyProfile = () => {
  const { showToast } = useToastStore.getState();

  return useQuery({
    queryKey: ["myProfile"],
    queryFn: async () => {
      const storedUser = localStorage.getItem("userProfile");
      if (storedUser) return JSON.parse(storedUser); // ✅ localStorage 활용

      const response = await fetchMyProfile();
      if (response.success) {
        localStorage.setItem("userProfile", JSON.stringify(response.data));
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
