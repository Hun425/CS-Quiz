import { create } from "zustand";
import { persist } from "zustand/middleware";
import { useProfileStore } from "@/store/profileStore";

interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  refreshToken: string | null;
  expiresAt: number | null;
  setToken: (token: string, refreshToken: string, expiresAt: number) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      isAuthenticated: false,
      token: null,
      refreshToken: null,
      expiresAt: null,

      // ✅ 로그인 성공 시 상태 및 로컬 스토리지에 저장
      setToken: (token, refreshToken, expiresAt) => {
        localStorage.setItem("access_token", token);
        localStorage.setItem("refresh_token", refreshToken);
        localStorage.setItem("expires_in", expiresAt.toString());

        set({
          isAuthenticated: true,
          token,
          refreshToken,
          expiresAt,
        });
      },

      // ✅ 로그아웃 시 상태 및 로컬 스토리지 정리
      logout: () => {
        useProfileStore.getState().clearProfile();
        localStorage.removeItem("access_token");
        localStorage.removeItem("refresh_token");
        localStorage.removeItem("expires_in");

        set({
          isAuthenticated: false,
          token: null,
          refreshToken: null,
          expiresAt: null,
        });
      },
    }),
    {
      name: "auth",
    }
  )
);
