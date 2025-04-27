"use client";

import { useEffect, useState } from "react";
import { useBattleSocketStore } from "@/store/battleStore";
import SubmitAnswerButton from "@/app/battles/_components/SubmitAnswerButton";

const BattleQuestion = () => {
  const nextQuestion = useBattleSocketStore((s) => s.nextQuestion);
  const startPayload = useBattleSocketStore((s) => s.startPayload);
  const progress = useBattleSocketStore((s) => s.progress);

  const currentQuestion = nextQuestion ?? startPayload?.firstQuestion ?? null;
  const roomId = startPayload?.roomId ?? null;
  const currentIndex = progress?.currentQuestionIndex
    ? progress.currentQuestionIndex + 1
    : 1;

  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [timeLeft, setTimeLeft] = useState<number>(0);

  useEffect(() => {
    if (!currentQuestion || !roomId) return;

    const key = `battle-timer-${roomId}`;
    const now = Date.now();

    let startedAt: number;
    const saved = sessionStorage.getItem(key);

    if (saved) {
      const parsed = JSON.parse(saved);
      if (parsed.currentQuestionId === currentQuestion.questionId) {
        startedAt = parsed.startedAt;
      } else {
        startedAt = now;
        sessionStorage.setItem(
          key,
          JSON.stringify({
            currentQuestionId: currentQuestion.questionId,
            startedAt,
          })
        );
      }
    } else {
      startedAt = now;
      sessionStorage.setItem(
        key,
        JSON.stringify({
          currentQuestionId: currentQuestion.questionId,
          startedAt,
        })
      );
    }

    const getRemainingTime = () =>
      Math.max(
        currentQuestion.timeLimit - Math.floor((Date.now() - startedAt) / 1000),
        0
      );

    setTimeLeft(getRemainingTime());

    const timer = setInterval(() => {
      const remaining = getRemainingTime();
      setTimeLeft(remaining);
      if (remaining <= 0) clearInterval(timer);
    }, 1000);

    return () => clearInterval(timer);
  }, [currentQuestion, roomId]);

  if (!currentQuestion) return null;

  return (
    <div className="bg-card-background rounded-2xl shadow-md p-4 space-y-4 md:p-6">
      {/* ✅ 문제 메타 정보 */}
      <div className="flex flex-row justify-between items-center text-sm text-muted gap-1">
        <span>
          {currentIndex}번 배점: {currentQuestion.points ?? 0}점
        </span>

        <span>⏳ 남은 시간: {timeLeft}s</span>
      </div>

      {/* ✅ 프로그레스 바 */}
      <div className="relative w-full h-2 bg-border rounded-full overflow-hidden">
        <div
          className="h-full bg-primary transition-all duration-1000 ease-linear"
          style={{
            width: `${(timeLeft / currentQuestion.timeLimit) * 100}%`,
          }}
        />
      </div>

      {/* ✅ 문제 내용 */}
      <p className="text-base sm:text-lg font-medium text-foreground">
        {currentQuestion.questionText}
      </p>

      {/* ✅ 선택지 */}
      <ul className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3">
        {(currentQuestion.options ?? []).map((option, idx) => (
          <li
            key={idx}
            onClick={() => setSelectedOption(option)}
            className={`px-3 py-2 sm:px-4 sm:py-3 rounded-lg border text-center cursor-pointer transition text-foreground break-keep
        ${
          selectedOption === option
            ? "bg-primary text-white border-primary"
            : "hover:bg-card-hover"
        }`}
          >
            <span className="inline-block w-full text-sm sm:text-base truncate">
              {option}
            </span>
          </li>
        ))}
      </ul>

      {/* ✅ 제출 영역 */}
      <div className="flex flex-col sm:flex-row justify-between items-center pt-4 gap-2">
        <SubmitAnswerButton
          questionId={currentQuestion.questionId}
          answer={selectedOption ?? ""}
          timeSpentSecond={currentQuestion.timeLimit - timeLeft}
        />
      </div>
      <p className="text-sm text-muted">
        ✨ 한번 제출하면 답을 변경할 수 없어요.
      </p>
    </div>
  );
};

export default BattleQuestion;
