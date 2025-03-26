"use client";

import { useEffect, useState, memo } from "react";
import { useAuthStore } from "@/store/authStore";

const TokenTimer = memo(() => {
  const expiresAt = useAuthStore((state) => state.expiresAt);
  const logout = useAuthStore((state) => state.logout);
  const [timeLeft, setTimeLeft] = useState<number | null>(null);

  useEffect(() => {
    if (typeof window === "undefined") return;

    if (!expiresAt) return;

    const updateTimer = () => {
      const now = Date.now();
      const remainingTime = expiresAt - now;

      if (remainingTime <= 0) {
        logout();
      } else {
        setTimeLeft(Math.ceil(remainingTime / 1000));
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
        ? `${Math.floor(timeLeft / 60)}분 ${timeLeft % 60}초`
        : "만료 정보 없음"}
    </span>
  );
});

TokenTimer.displayName = "TokenTimer";

export default TokenTimer;
