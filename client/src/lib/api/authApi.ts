// 인증 관련 API
import httpClient from "../httpClient";
import { useAuthStore } from "@/store/authStore";

/**
 * 🔹 OAuth2 로그인 요청의 경우 page redirect 방식이므로 별도의 API 요청 필요 없음
 */
export const authApi = {
  /**
   * 🔹 토큰 갱신 (Refresh Token 사용)
   * - `httpOnly` 쿠키 기반 인증이므로 Authorization 헤더 불필요
   */
  // refreshToken: async () => {
  //   try {
  //     const response = await httpClient.post("/api/oauth2/refresh");
  //     return response.data;
  //   } catch (error) {
  //     console.error("🔴 토큰 갱신 실패:", error);
  //     throw error;
  //   }
  // },

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
