/**
 * ⏳ 남은 시간을 "MM:SS" 형식으로 변환하는 함수
 * @param timeLeft 남은 시간 (초 단위)
 * @returns "MM:SS" 형식의 문자열
 */
export const formatTimeLeft = (seconds: number) => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(
    remainingSeconds
  ).padStart(2, "0")}`;
};
