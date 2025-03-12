"use client";
import { useEffect } from "react";
import { useAuthStore } from "@/store/authStore";
import { useRouter, useSearchParams } from "next/navigation";
import { getUserProfile } from "@/lib/api/userApi";

export default function AuthCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const setAuthenticated = useAuthStore((state) => state.setAuthenticated);

  useEffect(() => {
    const token = searchParams.get("token");
    const refreshToken = searchParams.get("refreshToken");
    const expiresIn = searchParams.get("expiresIn");

    if (token && refreshToken) {
      // ✅ JWT를 localStorage에 저장
      localStorage.setItem("access_token", token);
      localStorage.setItem("refresh_token", refreshToken);
      localStorage.setItem("expires_in", expiresIn || "3600");

      // ✅ 인증 상태 변경
      setAuthenticated(true);
      getUserProfile();

      // ✅리다이렉트
      router.replace("/quizzes");
    } else {
      console.warn("🔴 잘못된 로그인 응답. 로그인 페이지로 이동.");
      router.replace("/login?error=invalid_token");
    }
  }, [searchParams, router, setAuthenticated]);

  return (
    <div className="flex flex-col items-center justify-center h-screen space-y-4">
      <div className="w-12 h-12 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
      <p className="text-lg text-gray-600 animate-pulse">로그인 처리 중...</p>
    </div>
  );
}
