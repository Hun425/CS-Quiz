"use client";

import { useState, useEffect } from "react";
import Loading from "@/app/_components/Loading";
import SubmitAnswerButton from "@/app/battles/_components/SubmitAnswerButton";
import { useBattleSocketStore } from "@/store/battleStore";
import { BattleNextQuestionResponse } from "@/lib/types/battle";

const BattleContent = () => {
  const { nextQuestion, startPayload, progress } = useBattleSocketStore();

  const [currentQuestion, setCurrentQuestion] =
    useState<BattleNextQuestionResponse | null>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [timeSpent] = useState<number>(5);
  const [lastUpdated, setLastUpdated] = useState<number>(Date.now());

  // 다음 문제 또는 시작 시점의 첫 번째 문제 수신 시 갱신
  useEffect(() => {
    if (nextQuestion) {
      setCurrentQuestion(nextQuestion);
      setSelectedOption(null);
      setLastUpdated(Date.now());
    } else if (startPayload?.firstQuestion) {
      setCurrentQuestion(startPayload.firstQuestion);
      setSelectedOption(null);
      setLastUpdated(Date.now());
    }
  }, [nextQuestion, startPayload]);

  // 일정 시간 동안 무응답 시 fallback 처리
  useEffect(() => {
    const interval = setInterval(() => {
      const now = Date.now();
      if (now - lastUpdated > 15000) {
        console.warn("⏰ 서버 응답 없음. fallback 처리 시도");
        // 필요한 fallback 예시: 새로고침 또는 진행상황 요청 등
        location.reload();
      }
    }, 5000);

    return () => clearInterval(interval);
  }, [lastUpdated]);

  if (!currentQuestion || !progress) return <Loading />;

  const progressList = Object.values(progress.participantProgress);
  const topScorer = progressList.reduce((a, b) =>
    a.currentScore > b.currentScore ? a : b
  );

  return (
    <div className="max-w-screen-lg mx-auto px-4 py-6 min-h-screen bg-background flex flex-col md:flex-row md:gap-6">
      <div className="flex-1 space-y-6">
        <div className="text-xl md:text-2xl font-bold">
          문제 {progress.currentQuestionIndex + 1} / {progress.totalQuestions}
        </div>

        <div className="text-sm text-muted">
          배점: {currentQuestion.points}점 · 제한 시간:{" "}
          {currentQuestion.timeLimit}s
        </div>

        <div className="bg-card-background rounded-2xl shadow-md p-4 md:p-6 space-y-4">
          <p className="text-base md:text-lg font-medium text-foreground">
            {currentQuestion.questionText}
          </p>

          <ul className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {currentQuestion.options?.map((option, idx) => (
              <li
                key={idx}
                onClick={() => setSelectedOption(option)}
                className={`p-3 rounded-xl border text-center cursor-pointer transition text-foreground
                  ${
                    selectedOption === option
                      ? "bg-primary text-white border-primary"
                      : "hover:bg-card-hover"
                  }`}
              >
                {option}
              </li>
            ))}
          </ul>

          {selectedOption && (
            <div className="flex justify-end pt-4">
              <SubmitAnswerButton
                questionId={currentQuestion.questionId}
                answer={selectedOption}
                timeSpentSecond={timeSpent}
              />
            </div>
          )}
        </div>

        {/* 모바일용 사용자 요약 */}
        <div className="block md:hidden">
          <div className="text-sm font-semibold text-muted mb-2">
            참가자 목록
          </div>
          <div className="flex flex-wrap gap-2">
            {progressList.map((p) => (
              <div
                key={p.participantId}
                className={`w-10 h-10 rounded-full flex items-center justify-center text-xs font-medium bg-white border
                  ${
                    p.participantId === topScorer.participantId
                      ? "border-yellow-400"
                      : "border-border"
                  }`}
              >
                {p.username.charAt(0)}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 데스크탑용 오른쪽 사이드바 */}
      <aside className="hidden md:block w-full md:w-80 bg-sub-background rounded-xl p-4 space-y-4">
        <h2 className="text-lg font-semibold text-foreground">
          참가자 진행 상황
        </h2>
        <ul className="space-y-2">
          {progressList.map((p) => (
            <li
              key={p.participantId}
              className={`bg-white rounded-lg p-3 shadow flex flex-col gap-1 border
                ${
                  p.participantId === topScorer.participantId
                    ? "border-yellow-400"
                    : "border-border"
                }`}
            >
              <div className="font-medium flex items-center gap-1">
                {p.username}
                {p.participantId === topScorer.participantId && (
                  <span className="text-yellow-500">⭐</span>
                )}
              </div>
              <div className="text-sm text-muted">
                점수: {p.currentScore}점 · 정답률: {p.correctRate}% · 평균:{" "}
                {p.averageAnswerTime}s
              </div>
            </li>
          ))}
        </ul>
      </aside>
    </div>
  );
};

const BattleRoomClientPage = () => {
  return <BattleContent />;
};

export default BattleRoomClientPage;
