"use client";
import { useEffect } from "react";
import { useAuthStore } from "@/store/authStore";
import { useRouter, useSearchParams } from "next/navigation";
import { useGetMyProfile } from "@/lib/api/user/useGetMyProfile";

export default function AuthCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { isAuthenticated, setToken } = useAuthStore();

  // ✅ 로그인 토큰 저장 및 인증 상태 업데이트
  useEffect(() => {
    const token = searchParams.get("token");
    const refreshToken = searchParams.get("refreshToken");
    const expiresIn = searchParams.get("expiresIn"); //기본 한시간 후 만료됨

    if (token && refreshToken && expiresIn) {
      const expiresAt = Date.now() + Number(expiresIn);

      // ✅ Zustand 상태에 저장 (localStorage도 자동 반영됨)
      setToken(token, refreshToken, expiresAt);
    } else {
      console.warn("🔴 잘못된 로그인 응답. 로그인 페이지로 이동.");
      router.replace("/login");
    }
  }, [searchParams, setToken, router]);

  // ✅ 인증 상태가 true일 때만 내 프로필 조회
  const { isLoading, data: userProfile } = useGetMyProfile();

  // ✅ 프로필이 성공적으로 로드되면 리다이렉트 실행
  useEffect(() => {
    if (isAuthenticated && userProfile) {
      router.replace("/quizzes");
    }
  }, [isAuthenticated, userProfile, router]);

  return (
    <div className="flex flex-col items-center justify-center h-screen space-y-4">
      <div className="w-12 h-12 border-4 border-gray-300 border-t-blue-600 rounded-full animate-spin"></div>
      <p className="text-lg text-gray-600 animate-pulse">
        {isLoading ? "프로필 불러오는 중..." : "로그인 처리 중..."}
      </p>
    </div>
  );
}
