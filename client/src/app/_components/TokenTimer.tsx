"use client";

import { useEffect, useState, useRef, memo } from "react";
import { useAuthStore } from "@/store/authStore";
import refreshAccessToken from "@/lib/api/refreshAccessToken";

const TokenTimer = memo(() => {
  const expiresAt = useAuthStore((state) => state.expiresAt);
  const logout = useAuthStore((state) => state.logout);
  const [timeLeft, setTimeLeft] = useState<number | null>(null);
  const alertShownRef = useRef(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (!expiresAt) return;

    const updateTimer = () => {
      const now = Date.now();
      const remainingMs = expiresAt - now;
      const remainingSec = Math.max(0, Math.ceil(remainingMs / 1000));

      setTimeLeft(remainingSec);

      // 토큰 만료
      if (remainingSec === 0) {
        logout();
        alertShownRef.current = false; // 다음에 새로 로그인하면 다시 alert 가능하도록 초기화
        return;
      }

      // 5분 이하이고 alert가 아직 안 띄워졌으면
      if (remainingSec <= 300 && !alertShownRef.current) {
        alertShownRef.current = true;

        if (window.confirm("토큰이 곧 만료됩니다. 연장하시겠습니까?")) {
          refreshAccessToken();
        }
      }
    };

    updateTimer(); // 초기 실행
    const intervalId = setInterval(updateTimer, 1000);

    return () => clearInterval(intervalId);
  }, [expiresAt, logout]);

  return (
    <span
      className={`text-sm min-w-10 ${
        timeLeft !== null && timeLeft <= 300
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
