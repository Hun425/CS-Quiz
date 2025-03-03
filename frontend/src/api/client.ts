// src/api/client.ts
import axios, {
    AxiosInstance,
    AxiosRequestConfig,
    AxiosResponse,
    AxiosError,
    InternalAxiosRequestConfig
} from 'axios';
import { useAuthStore } from '../store/authStore';
import { refreshAccessToken } from '../utils/authUtils';

// 요청 구성 확장 타입
interface ExtendedAxiosRequestConfig extends InternalAxiosRequestConfig {
    _retry?: boolean;
}

// 백엔드 API 기본 URL 설정
const BASE_URL = 'http://localhost:8080/api';

// 기본 API 클라이언트 설정
const apiClient: AxiosInstance = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 요청 인터셉터
apiClient.interceptors.request.use(
    async (config: InternalAxiosRequestConfig): Promise<InternalAxiosRequestConfig> => {
        // 인증 상태 확인
        const { isAuthenticated, token, expiresAt } = useAuthStore.getState();
        const isTokenExpired = expiresAt && expiresAt < Date.now();

        // 인증이 필요한 요청이고 토큰이 만료된 경우 갱신 시도
        if (isAuthenticated && isTokenExpired && config.url !== '/oauth2/refresh') {
            const refreshed = await refreshAccessToken();
            if (refreshed) {
                // 토큰 갱신 성공, 새 토큰으로 헤더 설정
                const newToken = useAuthStore.getState().token;
                config.headers.set('Authorization', `Bearer ${newToken}`);
            } else {
                // 토큰 갱신 실패, 로그아웃 처리
                useAuthStore.getState().logout();
            }
        } else if (isAuthenticated && token) {
            // 토큰이 유효하면 헤더에 추가
            config.headers.set('Authorization', `Bearer ${token}`);
        }

        return config;
    },
    (error: Error): Promise<Error> => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
    (response: AxiosResponse): AxiosResponse => response,
    async (error: AxiosError): Promise<unknown> => {
        const originalRequest = error.config as ExtendedAxiosRequestConfig;

        // 인증 오류가 발생하고, 토큰 갱신 시도가 아직 이루어지지 않은 경우
        if (
            error.response?.status === 401 &&
            originalRequest &&
            !originalRequest._retry &&
            originalRequest.url !== '/oauth2/refresh'
        ) {
            originalRequest._retry = true;

            // 토큰 갱신 시도
            const refreshed = await refreshAccessToken();
            if (refreshed) {
                // 토큰 갱신 성공, 원래 요청 재시도
                const newToken = useAuthStore.getState().token;
                originalRequest.headers.set('Authorization', `Bearer ${newToken}`);
                return apiClient(originalRequest);
            }
        }

        // 토큰 갱신 실패 또는 다른 오류
        return Promise.reject(error);
    }
);

// API 호출 헬퍼 함수
export const api = {
    get: <T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.get<T>(url, config);
    },
    post: <T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.post<T>(url, data, config);
    },
    put: <T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.put<T>(url, data, config);
    },
    delete: <T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.delete<T>(url, config);
    },
};

export default apiClient;