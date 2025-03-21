// 인증 관련 API
import httpClient from "./httpClient";
import { useAuthStore } from "@/store/authStore";

/**
 * 🔹 OAuth2 로그인 요청의 경우 page redirect 방식이므로 별도의 API 요청 필요 없음
 */
export const authApi = {
  /**
   * 🔹 로그아웃
   */
  logout: async () => {
    try {
      await httpClient.post("/auth/logout", null);
      useAuthStore.getState().logout();
      window.location.href = "/";
    } catch (error) {
      console.error("🔴 로그아웃 요청 실패:", error);
    }
  },
};
