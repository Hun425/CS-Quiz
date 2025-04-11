"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";

/**
 * 인증되지 않은 경우 alert 후 로그인 페이지로 리디렉션하는 훅
 */
export const useProtectedRoute = () => {
  const router = useRouter();
  const { isAuthenticated, expiresAt } = useAuthStore();

  const isValidSession =
    isAuthenticated && !!expiresAt && Date.now() < expiresAt;

  useEffect(() => {
    if (!isValidSession) {
      alert("로그인이 필요합니다.");
      router.replace("/login");
    }
  }, [isValidSession, router]);

  return { isAuthenticated: isValidSession };
};
