import { useEffect, useRef, useState } from "react";
import { useQuizStore } from "@/store/quizStore";

interface TimerProps {
  onTimeUp: () => void; // 시간이 다 됐을 때 실행할 콜백
}

const Timer: React.FC<TimerProps> = ({ onTimeUp }) => {
  // 상태에서 endTime 가져오기
  const { endTime } = useQuizStore();

  // 남은 시간 상태: endTime이 있다면 그 차이를 초 단위로 계산, 없으면 0
  const [timeLeft, setTimeLeft] = useState(() => {
    return endTime ? Math.max(0, Math.floor((endTime - Date.now()) / 1000)) : 0;
  });

  // 중복 타이머 방지용 ref
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (!endTime) return;

    const tick = () => {
      // 남은 시간 계산 (초 단위)
      const newTimeLeft = Math.max(
        0,
        Math.floor((endTime - Date.now()) / 1000)
      );
      setTimeLeft(newTimeLeft);

      // 시간이 다 되었을 경우
      if (newTimeLeft <= 0 && intervalRef.current) {
        clearInterval(intervalRef.current); // 타이머 정지
        intervalRef.current = null;
        onTimeUp(); // 콜백 실행
      }
    };

    // 중복 타이머 방지: interval이 없다면 생성
    if (!intervalRef.current) {
      intervalRef.current = setInterval(tick, 1000); // 1초마다 tick 실행
    }

    tick(); // 최초 1회 즉시 실행 (1초 지연 방지)

    // 언마운트 시 타이머 정리
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [endTime, onTimeUp]);

  // 시:분 형식으로 변환하는 함수
  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${String(minutes).padStart(2, "0")}:${String(
      remainingSeconds
    ).padStart(2, "0")}`;
  };

  return (
    <p className="text-lg font-bold text-red-500">
      ⏳ {formatTime(timeLeft)} {/* 남은 시간 출력 */}
    </p>
  );
};

export default Timer;
