"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useBattleSocketStore } from "@/store/battleStore";
import { useBattleSocket } from "@/lib/services/websocket/useBattleSocket";
// import { useBattleHealthCheck } from "@/lib/services/websocket/useBattleHealthCheck";
import { BattleStatus, BattleNextQuestionResponse } from "@/lib/types/battle";
import Loading from "@/app/_components/Loading";
import SubmitAnswerButton from "@/app/battles/_components/SubmitAnswerButton";
import BattleParticipantsList from "./BattleParticipantsList";

const BattleContent = () => {
  const { roomid } = useParams();
  const router = useRouter();
  const roomId = Number(roomid);
  useBattleSocket(roomId);
  // useBattleHealthCheck(roomId);

  const {
    nextQuestion,
    startPayload,
    status,
    endPayload,
    participantsPayload,
  } = useBattleSocketStore();

  const [currentQuestion, setCurrentQuestion] =
    useState<BattleNextQuestionResponse | null>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [timeSpent] = useState<number>(5);

  const participants = participantsPayload?.participants || [];

  const isLoading = !(
    status === BattleStatus.READY || status === BattleStatus.IN_PROGRESS
  );

  const isDisconnected =
    !participantsPayload || !currentQuestion || status === undefined;

  useEffect(() => {
    if (isDisconnected) {
      setCurrentQuestion(null);
      setSelectedOption(null);

      const timeout = setTimeout(() => {
        router.replace("/battles");
      }, 5000); // 5초 후 자동 이동

      return () => clearTimeout(timeout);
    }

    if (nextQuestion) {
      setCurrentQuestion(nextQuestion);
      setSelectedOption(null);
    } else if (
      startPayload?.firstQuestion &&
      (status === BattleStatus.READY || status === BattleStatus.IN_PROGRESS)
    ) {
      setCurrentQuestion(startPayload.firstQuestion);
      setSelectedOption(null);
    }
  }, [nextQuestion, startPayload, status, isDisconnected, router]);

  useEffect(() => {
    if (status === BattleStatus.FINISHED && endPayload) {
      router.replace(`/battles/${roomId}/result`);
    }
  }, [status, endPayload, roomId, router]);

  if (isDisconnected) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen text-center space-y-3 text-danger">
        <h2 className="text-2xl font-semibold">⚠️ 연결이 끊어졌어요</h2>
        <p className="text-muted text-sm">
          서버와의 연결이 불안정하거나 종료되었어요.
          <br />
          잠시 후 배틀 목록으로 돌아갑니다...
        </p>
        <button
          onClick={() => router.replace("/battles")}
          className="mt-4 px-4 py-2 rounded-md bg-primary text-white hover:bg-primary/90 transition"
        >
          지금 이동하기
        </button>
      </div>
    );
  }

  if (isLoading) return <Loading />;

  return (
    <div className="max-w-screen-xl mx-auto px-4 py-8 min-h-screen grid grid-cols-1 sm:grid-cols-[2fr_1fr] gap-6">
      {/* 🧩 문제 영역 */}
      <div className="bg-card-background rounded-2xl shadow-md p-4 md:p-6 space-y-4">
        <div className="text-sm text-muted">
          배점: {currentQuestion.points}점 · 제한 시간:{" "}
          {currentQuestion.timeLimit}s
        </div>

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

      {/* 📱 모바일용 참가자 목록 */}
      <div className="block md:hidden mt-2">
        <BattleParticipantsList participants={participants} />
      </div>
      {/* 🖥 데스크탑용 참가자 목록 */}
      <div className="hidden md:block w-full sticky top-24 h-fit">
        <BattleParticipantsList participants={participants} />
      </div>
    </div>
  );
};

const BattlePlayPage = () => {
  return <BattleContent />;
};

export default BattlePlayPage;
