import { useMutation, useQueryClient } from "@tanstack/react-query";
import httpClient from "@/lib/api/httpClient";
import { UserProfile } from "@/lib/types/user";

// ✅ 사용자 프로필 업데이트 API
const updateProfile = async (userData: {
  username?: string;
  profileImage?: string;
}) => {
  const response = await httpClient.put<{
    success: boolean;
    data: UserProfile;
  }>("/users/me/profile", userData);
  return response.data;
};

// ✅ 사용자 프로필 업데이트 훅
export const useUpdateProfile = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateProfile,
    onSuccess: (updatedProfile) => {
      localStorage.setItem("userProfile", JSON.stringify(updatedProfile));
      queryClient.invalidateQueries({ queryKey: ["userProfile"] });
      queryClient.invalidateQueries({ queryKey: ["userStatistics"] });
    },
  });
};
