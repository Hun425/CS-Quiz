"use client";

import { useEffect } from "react";
import { useSearchParams, useRouter } from "next/navigation";

const AuthCallbackPage = () => {
  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    const accessToken = searchParams.get("token");

    if (accessToken) {
      localStorage.setItem("token", accessToken);
      router.push("/dashboard"); // 로그인 후 이동할 페이지
    } else {
      router.push("/login"); // 로그인 실패 시 이동할 페이지
    }
  }, [searchParams, router]);

  return (
    <div className="flex justify-center items-center h-screen">
      <p className="text-primary">로그인 처리 중...</p>
    </div>
  );
};

export default AuthCallbackPage;
