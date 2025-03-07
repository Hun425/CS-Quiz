// 인증 관련 API
import apiClient from "@/lib/api/apiClient";
import { AuthResponse } from "@/types/api";

export const authApi = {
  getAuthInfo: async (token: string) => {
    return apiClient.get<{ success: boolean; data: any }>("/auth/verify", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  },

  // 토큰 갱신
  refreshToken: async (refreshToken: string) => {
    return apiClient.post<{ success: boolean; data: AuthResponse }>(
      "/oauth2/refresh",
      null,
      {
        headers: {
          Authorization: `${refreshToken}`,
        },
      }
    );
  },

  // 로그아웃
  logout: async () => {
    const token = localStorage.getItem("auth_token");
    if (!token) {
      localStorage.removeItem("auth_token");
      return Promise.resolve();
    }

    try {
      await apiClient.post("/auth/logout", null, {
        headers: { Authorization: `Bearer ${token}` },
      });
    } catch (error) {
      console.error("로그아웃 요청 실패:", error);
    }

    localStorage.removeItem("auth_token");
  },
};
