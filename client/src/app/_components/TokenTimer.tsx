"use client";

import { useEffect, useState, useRef, memo } from "react";
import { useAuthStore } from "@/store/authStore";
import refreshAccessToken from "@/lib/api/refreshAccessToken";

const TokenTimer = memo(() => {
  const expiresAt = useAuthStore((state) => state.expiresAt);
  const logout = useAuthStore((state) => state.logout);
  const [timeLeft, setTimeLeft] = useState<number | null>(null);
  const alertShownRef = useRef(false); // ✅ useRef로 alertShown 상태 관리

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (!expiresAt) return;

    const updateTimer = () => {
      const now = Date.now();
      const remainingTime = expiresAt - now; // ✅ 밀리초(ms) 단위

      if (remainingTime <= 0) {
        logout();
      } else {
        setTimeLeft(Math.ceil(remainingTime / 1000)); // ✅ 초 단위 변환
      }

      // ✅ 59분 50초 (3590초) 이하일 때 한 번만 confirm 실행 // 300: 5분
      if (remainingTime / 1000 <= 300 && !alertShownRef.current) {
        alertShownRef.current = true;
        if (window.confirm("토큰이 곧 만료됩니다. 연장하시겠습니까?")) {
          refreshAccessToken();
        }
      }
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);

    return () => clearInterval(interval);
  }, [expiresAt, logout]);

  return (
    <span
      className={`text-sm ${
        timeLeft !== null && timeLeft < 300
          ? "text-red-500 font-semibold"
          : "text-neutral-500"
      }`}
    >
      {timeLeft !== null
        ? `${Math.floor(timeLeft / 60)}:${String(timeLeft % 60).padStart(
            2,
            "0"
          )}`
        : "만료 정보 없음"}
    </span>
  );
});

TokenTimer.displayName = "TokenTimer";

export default TokenTimer;
