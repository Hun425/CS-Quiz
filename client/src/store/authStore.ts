import { create } from "zustand";
import { persist } from "zustand/middleware";
import { useProfileStore } from "@/store/profileStore";
interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  refreshToken: string | null;
  expiresAt: number | null;
  wasLoggedOut: boolean; // ✅ 로그아웃 여부 플래그
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
      wasLoggedOut: false,

      setToken: (token, refreshToken, expiresAt) => {
        set({
          isAuthenticated: true,
          token,
          refreshToken,
          expiresAt,
          wasLoggedOut: false,
        });
      },

      logout: () => {
        useProfileStore.getState().clearProfile();

        set({
          isAuthenticated: false,
          token: null,
          refreshToken: null,
          expiresAt: null,
          wasLoggedOut: true,
        });

        alert("로그아웃 되었습니다.");
        if (typeof window !== "undefined") {
          localStorage.removeItem("auth");
          localStorage.removeItem("profile");
          window.location.href = "/login";
        }
      },
    }),
    {
      name: "auth",
    }
  )
);
