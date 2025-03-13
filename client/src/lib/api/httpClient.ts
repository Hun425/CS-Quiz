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

// 요청 인터셉터: JWT를 요청 헤더에 추가
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

// 응답 인터셉터: 공통 에러 핸들링
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
  (error) => {
    const showToast = useToastStore.getState().showToast;

    // 🔴 401 Unauthorized 에러 처리
    if (error.response?.status === 401) {
      console.warn("🔴 인증 만료됨. 로그아웃 처리");
      useAuthStore.getState().logout();

      if (typeof window !== "undefined") {
        localStorage.removeItem("access_token");
        localStorage.removeItem("refresh_token");
        localStorage.removeItem("expires_in");
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
