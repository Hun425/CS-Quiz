import axios from "axios";
import { useAuthStore } from "@/store/authStore";
import { useToastStore } from "@/store/toastStore";
import refreshAccessToken from "./refreshAccessToken";

// 서버 사이드와 클라이언트 사이드를 구분하여 API URL 설정
const getBaseURL = () => {
  // 서버 사이드 렌더링시 내부 Docker 네트워크 사용
  if (typeof window === 'undefined') {
    return process.env.API_BASE_URL ? `${process.env.API_BASE_URL}/api` : "http://backend:8080/api";
  }
  // 클라이언트 사이드에서는 외부 URL 사용
  return process.env.NEXT_PUBLIC_API_BASE_URL 
    ? `${process.env.NEXT_PUBLIC_API_BASE_URL}/api`
    : "http://localhost:8080/api";
};

const baseURL = getBaseURL();

const httpClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

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

    if (response.data?.success === false) {
      showToast(response.data.message || "API 요청 실패", "warning");
    }

    return response;
  },

  async (error) => {
    const showToast = useToastStore.getState().showToast;
    const { logout } = useAuthStore.getState();

    const status = error.response?.status;

    if (status === 401) {
      showToast("인증 오류: 로그인이 필요합니다.", "warning");
      const newAccessToken = await refreshAccessToken();
      if (newAccessToken) {
        error.config.headers.Authorization = `Bearer ${newAccessToken}`;
        return httpClient(error.config);
      }

      console.warn("🚨 토큰 갱신 실패. 로그아웃 처리");
      logout();
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    } else {
      console.error("❌ API 오류:", error.response);

      const message =
        error.response?.data?.message || "서버 오류가 발생했습니다.";

      const toastType =
        status >= 500 ? "error" : status >= 400 ? "warning" : "info"; // fallback

      showToast(message, toastType);
    }

    return Promise.reject(error);
  }
);

export default httpClient;
