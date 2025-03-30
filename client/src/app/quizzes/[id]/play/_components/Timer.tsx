import { useEffect, useRef, useState } from "react";
import { useQuizStore } from "@/store/quizStore";

interface TimerProps {
  onTimeUp: () => void;
}

const Timer: React.FC<TimerProps> = ({ onTimeUp }) => {
  const { endTime } = useQuizStore();
  const [timeLeft, setTimeLeft] = useState(() => {
    return endTime ? Math.max(0, Math.floor((endTime - Date.now()) / 1000)) : 0;
  });

  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (!endTime) return;

    const tick = () => {
      const newTimeLeft = Math.max(
        0,
        Math.floor((endTime - Date.now()) / 1000)
      );
      setTimeLeft(newTimeLeft);

      if (newTimeLeft <= 0 && intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
        onTimeUp();
      }
    };

    // 이미 타이머가 돌아가는 중이면 중복 생성 방지
    if (!intervalRef.current) {
      intervalRef.current = setInterval(tick, 1000);
    }

    // 초기 실행 한 번
    tick();

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [endTime, onTimeUp]);

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${String(minutes).padStart(2, "0")}:${String(
      remainingSeconds
    ).padStart(2, "0")}`;
  };

  return (
    <p className="text-lg font-bold text-red-500">⏳ {formatTime(timeLeft)}</p>
  );
};

export default Timer;
