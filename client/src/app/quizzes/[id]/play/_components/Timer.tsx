import { useEffect, useState } from "react";
import { useQuizStore } from "@/store/quizStore";

interface TimerProps {
  onTimeUp: () => void;
}

const Timer: React.FC<TimerProps> = ({ onTimeUp }) => {
  const { endTime } = useQuizStore();
  const [timeLeft, setTimeLeft] = useState(() => {
    return endTime ? Math.max(0, Math.floor((endTime - Date.now()) / 1000)) : 0;
  });

  useEffect(() => {
    const interval = setInterval(() => {
      const newTimeLeft = Math.max(
        0,
        Math.floor((endTime - Date.now()) / 1000)
      );
      setTimeLeft(newTimeLeft);

      if (newTimeLeft <= 0) {
        clearInterval(interval);
        onTimeUp();
      }
    }, 1000);

    return () => clearInterval(interval);
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
