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
      useAuthStore.getState().logout();

      // 2. 서버 쿠키 삭제
      await fetch("/api/auth/logout", {
        method: "POST",
      });

      window.location.href = "/";
    } catch (error) {
      console.error("🔴 로그아웃 요청 실패:", error);
    }
  },
};
