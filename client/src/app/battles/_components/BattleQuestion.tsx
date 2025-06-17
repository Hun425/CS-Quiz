"use client";

import { useEffect, useState } from "react";
import { useBattleSocketStore } from "@/store/battleStore";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";
import Button from "@/app/_components/Button";

const BattleQuestion = () => {
  const nextQuestion = useBattleSocketStore((s) => s.nextQuestion);
  const startPayload = useBattleSocketStore((s) => s.startPayload);

  const currentQuestion = nextQuestion ?? startPayload?.firstQuestion ?? null;
  const roomId = startPayload?.roomId ?? null;

  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [timeLeft, setTimeLeft] = useState<number>(0);
  const [submitted, setSubmitted] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(1);

  useEffect(() => {
    if (nextQuestion) {
      setCurrentIndex((prev) => prev + 1);
    } else {
      setCurrentIndex(1);
    }
  }, [nextQuestion]);

  useEffect(() => {
    setSubmitted(false);
    setSelectedOption(null);
  }, [currentQuestion]);

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

      if (remaining <= 0) {
        clearInterval(timer);
        if (roomId && currentQuestion) {
          battleSocketClient.forceNextQuestion();
          console.log("✅ 시간 초과로 강제 다음 문제 요청 보냄");
          setSubmitted(true);
        }
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [currentQuestion, roomId]);

  if (!currentQuestion) return null;

  const handleSubmit = () => {
    if (!submitted && selectedOption !== null) {
      battleSocketClient.submitAnswer(
        currentQuestion.questionId,
        selectedOption,
        currentQuestion.timeLimit - timeLeft
      );
      setSubmitted(true);
    }
  };

  return (
    <div className="relative">
      <div
        className={`bg-card-background rounded-2xl shadow-md p-4 space-y-4 md:p-6 relative`}
      >
        <div className="flex flex-row justify-between items-center text-sm text-muted gap-1">
          <span>
            {currentIndex}번 배점: {currentQuestion.points ?? 0}점
          </span>
          <span>⏳ 남은 시간: {timeLeft}s</span>
        </div>

        <div className="relative w-full h-2 bg-border rounded-full overflow-hidden">
          <div
            className="h-full bg-primary transition-all duration-1000 ease-linear"
            style={{
              width: `${(timeLeft / currentQuestion.timeLimit) * 100}%`,
            }}
          />
        </div>

        {/* 문제 (항상 맨 위에, 블러 영향 X) */}
        <p className="text-base sm:text-lg font-medium text-foreground relative z-20">
          {currentQuestion.questionText}
        </p>

        {/* 보기 (블러 영향 받음) */}
        <ul
          className={`grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3 ${
            submitted
              ? "pointer-events-none select-none backdrop-blur-[10px] bg-white/40 rounded-lg"
              : ""
          }`}
        >
          {(currentQuestion.options ?? []).map((option, idx) => (
            <li
              key={idx}
              onClick={() => {
                if (!submitted) {
                  setSelectedOption(option);
                }
              }}
              className={`px-3 py-2 sm:px-4 sm:py-3 rounded-lg border text-center cursor-pointer transition text-foreground break-keep ${
                selectedOption === option
                  ? "bg-primary text-white border-primary"
                  : "hover:bg-card-hover"
              }`}
            >
              <span className="inline-block w-full text-sm sm:text-base break-words">
                {option}
              </span>
            </li>
          ))}
        </ul>

        {/* 제출 버튼 */}
        <div className="flex flex-col sm:flex-row justify-between items-center pt-4 gap-2">
          <Button
            className="mt-4 px-4 py-2 bg-primary text-white rounded hover:opacity-90"
            onClick={handleSubmit}
            disabled={submitted}
          >
            {submitted ? "제출 완료" : "제출하기"}
          </Button>
          <p className="text-sm text-muted">
            ✨ 한번 제출하면 답을 변경할 수 없어요.
          </p>
        </div>
      </div>
    </div>
  );
};

export default BattleQuestion;
