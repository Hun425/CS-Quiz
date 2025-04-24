"use client";

import { useBattleSocketStore } from "@/store/battleStore";
import SubmitAnswerButton from "@/app/battles/_components/SubmitAnswerButton";
import { useState } from "react";

const BattleQuestion = () => {
  const currentQuestion = useBattleSocketStore(
    (s) => s.nextQuestion ?? s.startPayload?.firstQuestion ?? null
  );

  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [timeSpent] = useState<number>(5); // 추후 타이머로 변경 가능

  if (!currentQuestion) return null;

  return (
    <div className="bg-card-background rounded-2xl shadow-md p-4 md:p-6 space-y-4">
      <div className="text-sm text-muted">
        배점: {currentQuestion.points}점 · 제한 시간:{" "}
        {currentQuestion.timeLimit}s
      </div>

      <p className="text-base md:text-lg font-medium text-foreground">
        {currentQuestion.questionText}
      </p>

      <ul className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {currentQuestion.options.map((option, idx) => (
          <li
            key={idx}
            onClick={() => setSelectedOption(option)}
            className={`p-3 rounded-xl border text-center cursor-pointer transition text-foreground
              ${
                selectedOption === option
                  ? "bg-primary text-white border-primary"
                  : "hover:bg-card-hover"
              }
            `}
          >
            {option}
          </li>
        ))}
      </ul>

      <div className="flex flex-col md:flex-row justify-between items-center pt-4 gap-2">
        <p className="text-sm text-muted">
          ✨ 한번 제출하면 답을 변경할 수 없어요.
        </p>
        <SubmitAnswerButton
          questionId={currentQuestion.questionId}
          answer={selectedOption}
          timeSpentSecond={timeSpent}
        />
      </div>
    </div>
  );
};

export default BattleQuestion;
