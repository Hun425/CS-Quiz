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

      // ✅ 로그인 성공 시 상태 업데이트 (localStorage 조작 X)
      setToken: (token, refreshToken, expiresAt) => {
        set({
          isAuthenticated: true,
          token,
          refreshToken,
          expiresAt,
        });
      },

      // ✅ 로그아웃 시 상태 초기화 및 페이지 이동
      logout: () => {
        useProfileStore.getState().clearProfile();

        set({
          isAuthenticated: false,
          token: null,
          refreshToken: null,
          expiresAt: null,
        });

        alert("로그아웃 되었습니다."); // ✅ 알림 띄우기

        // ✅ 로그인 페이지로 이동
        if (typeof window !== "undefined") {
          window.location.href = "/login"; // 🚀 Next.js에서는 window.location.href 사용
        }
      },
    }),
    {
      name: "auth", // ✅ persist에 저장되는 key 값
    }
  )
);
