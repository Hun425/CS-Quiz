import axios from "axios";

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

//요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token"); // 클라이언트에서 토큰 가져오기
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);
apiClient.interceptors.response.use(
  (response) => response.data, // 성공 시 데이터만 반환
  (error) => {
    if (error.response?.status === 401) {
      // 인증 실패 시 로그아웃 처리 또는 로그인 페이지로 리다이렉트
      localStorage.removeItem("token");
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

//응답 인터셉터

export default apiClient;
