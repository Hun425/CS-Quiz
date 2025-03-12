import httpClient from "./httpClient";
import { useProfileStore } from "@/store/profileStore";
import { useToastStore } from "@/store/toastStore"; // ✅ Zustand Store 직접 사용
import { UserProfile } from "@/lib/types/user";

export const getUserProfile = async (userId?: number) => {
  const { showToast } = useToastStore.getState(); //
  const endpoint = userId ? `/users/${userId}/profile` : "/users/me/profile";

  try {
    const response = await httpClient.get<CommonApiResponse<UserProfile>>(
      endpoint
    );
    if (response.data.success) {
      console.log("✅ 프로필 조회 성공:", response);
      useProfileStore.getState().setUserProfile(response.data.data);
      showToast("프로필 조회 성공", "success");
      return response;
    } else {
      console.warn("⚠️ 프로필 조회 실패:", response.data);
      showToast("프로필 조회 실패", "warning"); // ✅ Zustand `showToast` 직접 사용
      return null;
    }
  } catch (error) {
    console.error("🔴 프로필 조회 오류:", error);
    showToast("프로필 조회 중 오류 발생", "error"); // ✅ 오류 발생 시 Toast
    return null;
  }
};

export const userApi = {
  // 사용자 통계 조회
  getUserStatistics: async (userId?: number) => {
    const endpoint = userId
      ? `/users/${userId}/statistics`
      : "/users/me/statistics";
    return httpClient.get<{ success: boolean; data: UserStatistics }>(endpoint);
  },

  // 최근 활동 조회
  getRecentActivities: async (userId?: number, limit: number = 10) => {
    const endpoint = userId
      ? `/users/${userId}/recent-activities?limit=${limit}`
      : `/users/me/recent-activities?limit=${limit}`;
    return httpClient.get<{ success: boolean; data: RecentActivity[] }>(
      endpoint
    );
  },

  // 업적 조회
  getAchievements: async (userId?: number) => {
    const endpoint = userId
      ? `/users/${userId}/achievements`
      : "/users/me/achievements";
    return httpClient.get<{ success: boolean; data: Achievement[] }>(endpoint);
  },

  // 주제별 성과 조회
  getTopicPerformance: async (userId?: number) => {
    const endpoint = userId
      ? `/users/${userId}/topic-performance`
      : "/users/me/topic-performance";
    return httpClient.get<{ success: boolean; data: TopicPerformance[] }>(
      endpoint
    );
  },

  // 프로필 정보 업데이트
  updateProfile: async (userData: {
    username?: string;
    profileImage?: string;
  }) => {
    return httpClient.put<{ success: boolean; data: UserProfile }>(
      "/users/me/profile",
      userData
    );
  },
};
