import { useQuery } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserProfile } from "@/lib/types/user";
import { useProfileStore } from "@/store/profileStore";
import { useToastStore } from "@/store/toastStore";
import { CommonApiResponse } from "@/lib/types/common"; // ✅ 공통 API 타입 사용

// ✅ 내 프로필 조회 API
const fetchMyProfile = async (): Promise<UserProfile> => {
  console.log("내 프로필 조회");
  const response = await httpClient.get<CommonApiResponse<UserProfile>>(
    "/users/me/profile"
  );

  return response.data.data; // ✅ success 여부는 인터셉터에서 처리되므로, data만 반환
};

// ✅ 내 프로필 조회 훅 (React Query)
export const useGetMyProfile = () => {
  const { showToast } = useToastStore.getState();

  return useQuery({
    queryKey: ["myProfile"],
    queryFn: async () => {
      // ✅ 스토어에서 기존 데이터가 있으면 반환
      const storedUser = useProfileStore.getState().userProfile;
      if (storedUser) return storedUser;

      // ✅ API 호출
      const userProfile = await fetchMyProfile();
      useProfileStore.getState().setUserProfile(userProfile);
      showToast("프로필 조회 성공", "success");

      return userProfile;
    },
    staleTime: 1000 * 60 * 10,
    refetchOnWindowFocus: true,
  });
};
