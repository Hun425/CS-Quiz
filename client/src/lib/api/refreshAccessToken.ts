import axios from "axios";
import { useAuthStore } from "@/store/authStore";

const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL
  ? `${process.env.NEXT_PUBLIC_API_BASE_URL}/api`
  : "http://localhost:8080/api";

/**
 * ✅ 액세스 토큰을 리프레시 토큰으로 재발급받는 유틸 함수
 * @returns 새 accessToken 또는 null
 */
// refreshAccessToken.ts
export default async function refreshAccessToken(): Promise<string | null> {
  const { refreshToken, setToken } = useAuthStore.getState();

  try {
    console.log("🔄 토큰 갱신 요청", refreshToken);
    if (!refreshToken) throw new Error("리프레시 토큰 없음");

    const response = await axios.post(
      `${baseURL}/oauth2/refresh`,
      {},
      {
        headers: {
          Authorization: `Bearer ${refreshToken.replace(/\s/g, "")}`,
        },
      }
    );

    console.log("✅ 토큰 갱신 성공", response.data);
    const {
      accessToken,
      refreshToken: newRefreshToken,
      expiresIn,
    } = response.data;

    if (!accessToken) throw new Error("잘못된 응답");

    const expiresAt = Date.now() + expiresIn * 1000;
    setToken(accessToken, newRefreshToken, expiresAt);

    return accessToken;
  } catch (err) {
    console.error("❌ 토큰 갱신 실패", err);
    return null;
  }
}
