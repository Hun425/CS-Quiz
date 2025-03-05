// src/api/authApi.ts - 인증 관련 API 연동
import axios from "axios";
import { AuthResponse } from "@/types/api";

const BASE_URL = "http://localhost:8080/api";

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export const authApi = {
  // 인증 정보 확인
  getAuthInfo: async (token: string) => {
    return apiClient.get<{ success: boolean; data: any }>("/auth/verify", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  },

  // 토큰 갱신
  refreshToken: async (refreshToken: string) => {
    return apiClient.post<{ success: boolean; data: AuthResponse }>(
      "/oauth2/refresh",
      null,
      {
        headers: {
          Authorization: `${refreshToken}`,
        },
      }
    );
  },

  // 로그아웃
  logout: async () => {
    const token = localStorage.getItem("auth_token");
    if (!token) return Promise.resolve();

    return apiClient.post("/auth/logout", null, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  },
};
