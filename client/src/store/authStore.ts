import { create } from "zustand";
import { persist } from "zustand/middleware";
import { useProfileStore } from "@/store/profileStore";

interface AuthState {
  isAuthenticated: boolean;
  setAuthenticated: (value: boolean) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      isAuthenticated: false,

      setAuthenticated: (value) => set({ isAuthenticated: value }),

      logout: () => {
        useProfileStore.getState().clearProfile();
        localStorage.removeItem("access_token");
        localStorage.removeItem("refresh_token");
        localStorage.removeItem("expires_in");
        set({ isAuthenticated: false });
      },
    }),
    {
      name: "auth",
    }
  )
);
