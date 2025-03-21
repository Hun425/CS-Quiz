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
const refreshAccessToken = async () => {
  try {
    const refreshToken = localStorage.getItem("refresh_token");
    if (!refreshToken) throw new Error("No refresh token found");

    const response = await axios.post(`${baseURL}/oauth2/refresh`, {
      refreshToken,
    });

    if (response.data?.accessToken) {
      // ✅ 새 토큰 저장
      localStorage.setItem("access_token", response.data.accessToken);
      localStorage.setItem("refresh_token", response.data.refreshToken);
      localStorage.setItem(
        "expires_in",
        (Date.now() + response.data.expiresIn * 1000).toString()
      );

      // ✅ Zustand 스토어 업데이트
      useAuthStore
        .getState()
        .setToken(
          response.data.accessToken,
          response.data.refreshToken,
          Date.now() + response.data.expiresIn * 1000
        );

      return response.data.accessToken;
    } else {
      throw new Error("Invalid refresh response");
    }
  } catch (error) {
    console.error("❌ 토큰 갱신 실패:", error);
    return null;
  }
};

// ✅ 요청 인터셉터: JWT를 요청 헤더에 추가
httpClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("access_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ✅ 응답 인터셉터: 401 에러 발생 시 토큰 갱신 후 재시도
httpClient.interceptors.response.use(
  (response) => {
    const showToast = useToastStore.getState().showToast;

    // ✅ API 요청은 성공했지만, `success: false`이면 Toast 띄우기
    if (response.data?.success === false) {
      console.warn("⚠️ API 요청 실패:", response);
      showToast(response.data.message || "API 요청 실패", "warning");
    }

    return response;
  },

  async (error) => {
    const showToast = useToastStore.getState().showToast;

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
      useAuthStore.getState().logout();
      localStorage.removeItem("access_token");
      localStorage.removeItem("refresh_token");
      localStorage.removeItem("expires_in");

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
