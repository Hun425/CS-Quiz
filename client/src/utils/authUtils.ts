// src/store/authStore.ts
import { create } from "zustand";
import { persist } from "zustand/middleware";
import { authApi } from "@/lib/api/authApi";

interface User {
  id: number;
  username: string;
  email: string;
  profileImage?: string;
  level: number;
}

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  expiresAt: number | null;
  login: (
    token: string,
    refreshToken: string,
    user: User,
    expiresIn: number
  ) => void;
  logout: () => void;
  updateUser: (userData: Partial<User>) => void;
  refreshAuth: () => Promise<boolean>;
  // 새로 추가되는 함수들
  isTokenExpired: () => boolean;
  updateTokens: (
    token: string,
    refreshToken: string,
    expiresIn: number
  ) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      isAuthenticated: false,
      user: null,
      token: null,
      refreshToken: null,
      expiresAt: null,

      login: (token, refreshToken, user, expiresIn) => {
        // Calculate expiration time in milliseconds
        const expiresAt = Date.now() + expiresIn;

        // Save token to localStorage (in addition to the persist middleware)
        localStorage.setItem("auth_token", token);
        localStorage.setItem("refresh_token", refreshToken);

        set({
          isAuthenticated: true,
          user,
          token,
          refreshToken,
          expiresAt,
        });
      },

      logout: async () => {
        try {
          // Call logout API
          await authApi.logout();
        } catch (error) {
          console.error("Logout error:", error);
        } finally {
          // Clear local storage
          localStorage.removeItem("auth_token");
          localStorage.removeItem("refresh_token");

          // Reset state
          set({
            isAuthenticated: false,
            user: null,
            token: null,
            refreshToken: null,
            expiresAt: null,
          });
        }
      },

      updateUser: (userData) => {
        const currentUser = get().user;
        if (!currentUser) return;

        set({
          user: { ...currentUser, ...userData },
        });
      },

      // 새로 추가하는 함수: 토큰 만료 여부 체크
      isTokenExpired: () => {
        const { expiresAt } = get();
        if (!expiresAt) return true;
        return Date.now() >= expiresAt;
      },

      // 새로 추가하는 함수: 토큰 업데이트
      updateTokens: (token, refreshToken, expiresIn) => {
        const expiresAt = Date.now() + expiresIn;

        // 로컬 스토리지 업데이트
        localStorage.setItem("auth_token", token);
        localStorage.setItem("refresh_token", refreshToken);

        // 상태 업데이트
        set({
          token,
          refreshToken,
          expiresAt,
        });
      },

      refreshAuth: async () => {
        const { refreshToken, expiresAt } = get();

        // Check if token is about to expire (within 5 minutes)
        const isExpiringSoon =
          expiresAt && expiresAt - Date.now() < 5 * 60 * 1000;

        if (refreshToken && isExpiringSoon) {
          try {
            const response = await authApi.refreshToken(refreshToken);

            if (response.data.success) {
              const authData = response.data.data;
              const user = get().user;

              if (user) {
                // login 대신 updateTokens 사용
                get().updateTokens(
                  authData.accessToken,
                  authData.refreshToken,
                  authData.expiresIn
                );
                return true;
              }
            }
          } catch (error) {
            console.error("Token refresh failed:", error);
            // If refresh fails, force logout
            get().logout();
            return false;
          }
        }

        // Return true if token is still valid
        return !!expiresAt && expiresAt > Date.now();
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
        expiresAt: state.expiresAt,
      }),
    }
  )
);

// Setup token refresh check on app initialization
export const initAuthRefresh = () => {
  const checkAndRefreshToken = async () => {
    const { refreshAuth, isAuthenticated } = useAuthStore.getState();

    if (isAuthenticated) {
      await refreshAuth();
    }
  };

  // Check on init
  checkAndRefreshToken();

  // Setup interval to check token (every 4 minutes)
  setInterval(checkAndRefreshToken, 4 * 60 * 1000);
};

/**
 * 사용자가 인증되었는지 확인합니다.
 * 토큰 만료 여부도 검사합니다.
 */
export const isAuthenticated = (): boolean => {
  const store = useAuthStore.getState();
  return store.isAuthenticated && !store.isTokenExpired();
};

/**
 * 액세스 토큰을 갱신합니다.
 * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 요청합니다.
 */
export const refreshAccessToken = async (): Promise<boolean> => {
  const store = useAuthStore.getState();
  return await store.refreshAuth();
};

/**
 * 인증이 필요한 URL로 이동하기 전에 현재 URL을 저장합니다.
 */
export const saveAuthRedirect = (path: string): void => {
  localStorage.setItem("authRedirect", path);
};

/**
 * 로그인 상태가 변경될 때 실행할 콜백을 등록합니다.
 */
export const onAuthStateChanged = (
  callback: (isAuthenticated: boolean) => void
): (() => void) => {
  // Zustand subscribe 메소드에 상태 변경 리스너 전달
  const unsubscribe = useAuthStore.subscribe((state, prevState) => {
    // 인증 상태가 변경되었을 때만 콜백 호출
    if (state.isAuthenticated !== prevState.isAuthenticated) {
      callback(state.isAuthenticated);
    }
  });

  return unsubscribe;
};
