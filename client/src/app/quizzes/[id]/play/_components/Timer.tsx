"use client";

import { useEffect, useState, useRef, memo } from "react";

interface TimerProps {
  initialTime: number;
  onTimeUp: () => void;
}

const Timer = memo(({ initialTime, onTimeUp }: TimerProps) => {
  const [timeLeft, setTimeLeft] = useState(initialTime);
  const endTimeRef = useRef<number | null>(null);

  useEffect(() => {
    if (typeof window === "undefined") return;

    // ✅ 초기화: 종료 시간을 설정
    const now = Date.now();
    endTimeRef.current = now + initialTime * 1000;

    const updateTimer = () => {
      if (!endTimeRef.current) return;

      const now = Date.now();
      const remainingTime = Math.ceil((endTimeRef.current - now) / 1000);

      if (remainingTime <= 0) {
        setTimeLeft(0);
        clearInterval(interval);
        onTimeUp();
      } else {
        setTimeLeft(remainingTime);
      }
    };

    // ✅ 1초마다 실행
    const interval = setInterval(updateTimer, 1000);
    updateTimer(); // 즉시 실행하여 화면이 1초 늦게 반영되는 문제 해결

    return () => clearInterval(interval);
  }, [initialTime, onTimeUp]);

  const formatTimeLeft = () => {
    if (timeLeft === null) return "00:00";

    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(
      2,
      "0"
    )}`;
  };

  return (
    <span
      className={`w-[140px] px-6 py-3 text-lg rounded-md font-bold text-center shadow-md
    ${
      timeLeft !== null && timeLeft <= 60
        ? "bg-red-500 text-white"
        : "bg-gray-800 text-white"
    }`}
    >
      ⏳ {formatTimeLeft()}
    </span>
  );
});

Timer.displayName = "Timer";

export default Timer;
