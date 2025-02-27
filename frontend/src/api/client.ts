// src/api/client.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// 기본 API 클라이언트 설정
const apiClient: AxiosInstance = axios.create({
    baseURL: 'http://localhost:8080', // 백엔드 서버 URL
    headers: {
        'Content-Type': 'application/json',
    },
});

// 요청 인터셉터
apiClient.interceptors.request.use(
    (config) => {
        // 로컬 스토리지에서 토큰 가져오기
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // 토큰 만료 처리 (401 Unauthorized)
        if (error.response.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                // 리프레시 토큰으로 새 액세스 토큰 요청
                const refreshToken = localStorage.getItem('refreshToken');
                const response = await axios.post('http://localhost:8080/api/oauth2/refresh', {}, {
                    headers: {
                        'Authorization': `Bearer ${refreshToken}`,
                    },
                });

                // 새 토큰 저장
                const { accessToken } = response.data;
                localStorage.setItem('accessToken', accessToken);

                // 원래 요청 헤더 업데이트
                originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
                return axios(originalRequest);
            } catch (refreshError) {
                // 리프레시 토큰도 만료된 경우, 로그아웃 처리
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                // 로그인 페이지로 리다이렉트 (추후 구현)
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

// API 호출 헬퍼 함수
export const api = {
    get: <T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.get<T>(url, config);
    },
    post: <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.post<T>(url, data, config);
    },
    put: <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.put<T>(url, data, config);
    },
    delete: <T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
        return apiClient.delete<T>(url, config);
    },
};

export default apiClient;