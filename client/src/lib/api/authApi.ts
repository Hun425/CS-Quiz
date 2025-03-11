// 인증 관련 API //API 비즈니스 로직 분리
import httpClient from "./httpClient";
import AuthResponse from "../types/auth";

export const authApi = {
  /**
   * 🔹 OAuth2 로그인 요청의 경우 page redirect 방식을 적용하기 때문에 별도의 fetch 요청 필요없음
 
  */
  /**
   * 🔹 인증 정보 확인 (토큰 검증)
   * @param token - JWT 액세스 토큰
   */
  getAuthInfo: async (token: string) => {
    try {
      const response = await httpClient.get<{
        success: boolean;
        data: AuthResponse;
      }>("/auth/verify", {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data;
    } catch (error) {
      console.error("🔴 인증 정보 조회 실패:", error);
      throw error;
    }
  },

  /**
   * 🔹 토큰 갱신 (Refresh Token 사용)
   * @param refreshToken - 갱신할 Refresh Token
   */
  refreshToken: async (refreshToken: string) => {
    try {
      const response = await httpClient.post<{
        success: boolean;
        data: AuthResponse;
      }>("/api/oauth2/refresh", null, {
        headers: {
          Authorization: `Bearer ${refreshToken}`,
        },
      });
      return response.data;
    } catch (error) {
      console.error("🔴 토큰 갱신 실패:", error);
      throw error;
    }
  },

  /**
   * 🔹 로그아웃 (토큰 삭제)
   */
  logout: async () => {
    const token = localStorage.getItem("auth_token");

    if (!token) {
      console.warn("⚠️ 로그아웃: 저장된 토큰이 없음");
      return;
    }

    try {
      await httpClient.post("/auth/logout", null, {
        headers: { Authorization: `Bearer ${token}` },
      });
    } catch (error) {
      console.error("🔴 로그아웃 요청 실패:", error);
    } finally {
      localStorage.removeItem("auth_token"); // ✅ 최종적으로 제거
    }
  },
};
