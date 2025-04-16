import axios from "axios";
import { useAuthStore } from "@/store/authStore";
import { useToastStore } from "@/store/toastStore";

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
  const showToast = useToastStore.getState().showToast;
  try {
    if (!refreshToken) throw new Error("리프레시 토큰 없음");

    const response = await axios.post(
      `${baseURL}/oauth2/refresh`,
      {},
      {
        headers: {
          "X-Refresh-Token": `Bearer ${refreshToken}`,
        },
      }
    );

    showToast("액세스 토큰이 갱신되었습니다.", "success");
    const {
      accessToken,
      refreshToken: newRefreshToken,
      expiresIn,
    } = response.data;

    if (!accessToken) throw new Error("잘못된 응답");

    const expiresAt = Date.now() + expiresIn;
    setToken(accessToken, newRefreshToken, expiresAt);

    return accessToken;
    // eslint-disable-next-line
  } catch (err) {
    showToast(
      "액세스 토큰 갱신에 실패했습니다. 다시 로그인 해주세요.",
      "error"
    );
    return null;
  }
}
