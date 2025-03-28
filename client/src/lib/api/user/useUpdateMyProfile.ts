import { useMutation, useQueryClient } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserProfile } from "@/lib/types/user";
import { useProfileStore } from "@/store/profileStore";

// ✅ 사용자 프로필 업데이트 API
const updateProfile = async (userData: {
  username?: string;
  profileImage?: string;
}) => {
  const response = await httpClient.put<CommonApiResponse<UserProfile>>(
    "/users/me/profile",
    userData
  );
  return response.data.data;
};

// ✅ 사용자 프로필 업데이트 훅
export const useUpdateMyProfile = () => {
  const queryClient = useQueryClient();
  const { setUserProfile } = useProfileStore();

  return useMutation({
    mutationFn: updateProfile,
    onSuccess: (updatedProfile) => {
      setUserProfile(updatedProfile);
      queryClient.invalidateQueries({ queryKey: ["myProfile"] });
      queryClient.invalidateQueries({ queryKey: ["userStatistics"] });
    },
  });
};
