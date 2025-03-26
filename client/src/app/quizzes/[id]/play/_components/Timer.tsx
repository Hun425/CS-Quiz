"use client";

import { useEffect, useState, useRef, memo } from "react";

interface TimerProps {
  initialTime: number;
  onTimeUp: () => void;
}

const Timer: React.FC<TimerProps> = ({ initialTime, onTimeUp }) => {
  const [timeLeft, setTimeLeft] = useState(initialTime);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (timeLeft <= 0) {
      onTimeUp();
      return;
    }

    timerRef.current = setInterval(() => {
      setTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  useEffect(() => {
    if (timeLeft <= 0 && timerRef.current) {
      clearInterval(timerRef.current);
    }
  }, [timeLeft]);

  const formatTimeLeft = () => {
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
    ${timeLeft <= 60 ? "bg-red-500 text-white" : "bg-gray-800 text-white"}`}
    >
      ⏳ {formatTimeLeft()}
    </span>
  );
};

export default memo(Timer);
