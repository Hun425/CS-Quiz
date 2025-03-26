import axios from "axios";
import { useAuthStore } from "@/store/authStore";
import { useToastStore } from "@/store/toastStore";

const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL
  ? `${process.env.NEXT_PUBLIC_API_BASE_URL}/api`
  : "http://localhost:8080/api";

const httpClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ 리프레시 토큰을 이용한 액세스 토큰 갱신 함수
export const refreshAccessToken = async () => {
  const { refreshToken, setToken, logout } = useAuthStore.getState();

  try {
    if (!refreshToken) {
      throw new Error("No refresh token found");
    }

    // ✅ Refresh Token을 Authorization 헤더에 담아서 전송
    const response = await axios.post(
      `${baseURL}/oauth2/refresh`,
      {},
      {
        headers: {
          Authorization: `Bearer ${refreshToken}`,
          Accept: "application/json",
        },
      }
    );

    if (response.data?.accessToken) {
      const newAccessToken = response.data.accessToken;
      const newRefreshToken = response.data.refreshToken;
      const expiresAt = Date.now() + response.data.expiresIn;

      // ✅ Zustand 스토어 업데이트
      setToken(newAccessToken, newRefreshToken, expiresAt);

      return newAccessToken;
    } else {
      throw new Error("Invalid refresh response");
    }
  } catch (error) {
    console.error("❌ 토큰 갱신 실패:", error);
    logout();
    return null;
  }
};

// ✅ 요청 인터셉터: JWT를 요청 헤더에 추가
httpClient.interceptors.request.use(
  (config) => {
    const { token } = useAuthStore.getState();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

httpClient.interceptors.response.use(
  (response) => {
    const showToast = useToastStore.getState().showToast;

    // ✅ API 요청은 성공했지만, `success: false`이면 Toast 띄우기
    if (response.data?.success === false) {
      showToast(response.data.message || "API 요청 실패", "warning");
    }

    return response;
  },

  async (error) => {
    const showToast = useToastStore.getState().showToast;
    const { logout } = useAuthStore.getState();

    // ✅ 401 Unauthorized 에러 처리
    if (error.response?.status === 401) {
      console.warn("🔴 인증 만료됨. 토큰 갱신 시도");

      const newAccessToken = await refreshAccessToken();
      if (newAccessToken) {
        // ✅ 새 토큰으로 기존 요청 재시도
        error.config.headers.Authorization = `Bearer ${newAccessToken}`;
        return httpClient(error.config);
      }

      console.warn("🚨 토큰 갱신 실패. 로그아웃 처리");
      logout();

      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    } else {
      // ❌ 기타 API 오류 처리 (예: 500, 403, 404 등)
      console.error("❌ API 오류:", error.response);
      showToast(
        error.response?.data?.message || "서버 오류가 발생했습니다.",
        "error"
      );
    }

    return Promise.reject(error);
  }
);

export default httpClient;
