"use client";

import { useAuthStore } from "@/store/authStore";
import { ComponentType } from "react";
import { useRouter } from "next/navigation";

export default function isAuth<P extends object>(
  WrappedComponent: ComponentType<P>
) {
  return function IsAuth(props: P) {
    const auth = useAuthStore((state) => state.isAuthenticated);
    const router = useRouter();

    if (!auth) {
      router.replace("/login"); // ✅ 클라이언트 환경에서는 `useRouter().replace()` 사용
      return null; // ✅ 리디렉트 후 렌더링을 방지
    }

    return <WrappedComponent {...props} />;
  };
}
