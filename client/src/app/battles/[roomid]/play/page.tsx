"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useBattleSocketStore } from "@/store/battleStore";
import { useBattleSocket } from "@/lib/services/websocket/useBattleSocket";
// import { useBattleHealthCheck } from "@/lib/services/websocket/useBattleHealthCheck";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";
import { BattleStatus, BattleNextQuestionResponse } from "@/lib/types/battle";
import Loading from "@/app/_components/Loading";
import SubmitAnswerButton from "@/app/battles/_components/SubmitAnswerButton";
// import BattleParticipantsList from "./BattleParticipantsList";

const BattleContent = () => {
  const { roomid } = useParams();
  const router = useRouter();
  const roomId = Number(roomid);

  // 소켓 연결 및 해제 시 leave 처리
  useBattleSocket(roomId);
  useEffect(() => {
    // 창이 닫히거나 컴포넌트 언마운트될 때 자동 나가기 처리
    return () => {
      battleSocketClient.leaveBattle();
    };
  }, []);

  const {
    nextQuestion,
    startPayload,
    status,
    endPayload,
    // participantsPayload,
  } = useBattleSocketStore();

  const [currentQuestion, setCurrentQuestion] =
    useState<BattleNextQuestionResponse | null>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [timeSpent] = useState<number>(5);

  // 로딩: READY 또는 IN_PROGRESS 상태가 아니면 로딩
  const isLoading =
    status !== BattleStatus.READY && status !== BattleStatus.IN_PROGRESS;

  // disconnected: 오직 상태가 undefined일 때만
  const isDisconnected = status === undefined;

  useEffect(() => {
    if (isDisconnected) {
      // 소켓 연결 전 로딩만
      return;
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
  }, [nextQuestion, startPayload, status, isDisconnected]);

  useEffect(() => {
    if (status === BattleStatus.FINISHED && endPayload) {
      router.replace(`/battles/${roomId}/result`);
    }
  }, [status, endPayload, roomId, router]);

  // 수동 나가기 버튼
  const handleLeave = () => {
    const ok = window.confirm(
      "정말 배틀을 나가시겠습니까? 바로 배틀 목록으로 이동합니다."
    );
    if (ok) {
      battleSocketClient.leaveBattle();
      router.replace("/battles");
    }
  };

  if (isLoading) return <Loading />;

  return (
    <div className="max-w-screen-xl mx-auto px-4 py-8 min-h-screen grid grid-cols-1 sm:grid-cols-[2fr_1fr] gap-6">
      {/* 나가기 버튼 영역 */}

      {/* 🧩 문제 영역 */}
      <div className="bg-card-background rounded-2xl shadow-md p-4 md:p-6 space-y-4">
        <div className="text-sm text-muted">
          배점: {currentQuestion?.points}점 · 제한 시간:{" "}
          {currentQuestion?.timeLimit}s
        </div>

        <p className="text-base md:text-lg font-medium text-foreground">
          {currentQuestion?.questionText}
        </p>

        <ul className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {currentQuestion?.options?.map((option, idx) => (
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
            questionId={currentQuestion?.questionId!}
            answer={selectedOption}
            timeSpentSecond={timeSpent}
          />
        </div>
      </div>

      <div className="col-span-full flex justify-end">
        <button
          onClick={handleLeave}
          className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 transition"
        >
          배틀 나가기
        </button>
      </div>
      {/* 참가자 목록 영역 (주석 처리) */}
      {/**
      <div className="block md:hidden mt-2">
        <BattleParticipantsList participants={participants} />
      </div>

      <div className="hidden md:block w-full sticky top-24 h-fit">
        <BattleParticipantsList participants={participants} />
      </div>
      **/}
    </div>
  );
};

const BattlePlayPage = () => {
  return <BattleContent />;
};

export default BattlePlayPage;
