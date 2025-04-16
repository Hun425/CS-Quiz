"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Loading from "@/app/_components/Loading";
import SubmitAnswerButton from "@/app/battles/_components/SubmitAnswerButton";
import { useBattleSocketStore } from "@/store/battleStore";
import { BattleStatus, BattleNextQuestionResponse } from "@/lib/types/battle";
import { useBattleSocket } from "@/lib/services/websocket/useBattleSocket";
import Image from "next/image";

const BattleContent = () => {
  const { roomId } = useParams();
  const router = useRouter();
  useBattleSocket(Number(roomId));

  const {
    nextQuestion,
    startPayload,
    progress,
    status,
    endPayload,
    participantsPayload,
  } = useBattleSocketStore();
  console.log("participantsPayload", participantsPayload);
  console.log(progress);
  const [currentQuestion, setCurrentQuestion] =
    useState<BattleNextQuestionResponse | null>(null);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [timeSpent] = useState<number>(5);

  useEffect(() => {
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
  }, [nextQuestion, startPayload, status]);

  useEffect(() => {
    if (status === BattleStatus.FINISHED && endPayload) {
      router.replace(`/battles/${roomId}/result`);
    }
  }, [status, endPayload, roomId, router]);

  const isLoading =
    !(status === BattleStatus.READY || status === BattleStatus.IN_PROGRESS) ||
    !currentQuestion;

  if (isLoading) return <Loading />;

  return (
    <div className="max-w-screen-lg mx-auto px-4 py-6 min-h-screen bg-background flex flex-col md:flex-row md:gap-6">
      <div className="flex-1 space-y-6">
        <div className="text-xl md:text-2xl font-bold">
          {/* 문제 {progress?.currentQuestionIndex + 1} / {progress?.totalQuestions} */}
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
        {Array.isArray(participantsPayload) &&
          participantsPayload.length > 0 && (
            <div className="block md:hidden">
              <div className="text-sm font-semibold text-muted mb-2">
                👥 참가자 목록
              </div>
              <div className="flex flex-wrap gap-2">
                {participantsPayload.map((p) => (
                  <div
                    key={p.userId}
                    className="px-3 py-2 rounded-full bg-white border border-border shadow text-sm text-foreground font-medium"
                  >
                    <Image
                      src={
                        p.profileImage
                          ? p.profileImage
                          : "/images/default_avatar.png"
                      }
                      alt={`${p.username} 프로필 이미지`}
                      className="w-6 h-6 rounded-full inline-block mr-2"
                      width={24}
                      height={24}
                    />
                    {p.username}
                  </div>
                ))}
              </div>
            </div>
          )}
      </div>

      {/* 데스크탑용 사이드바 */}
      {/* 
      <aside className="hidden md:block w-full md:w-80 bg-sub-background rounded-xl p-4 space-y-4">
        <h2 className="text-lg font-semibold text-foreground">참가자 진행 상황</h2>
        <ul className="space-y-2">
          {progressList.map((p) => (
            <li key={p.participantId} className="...">
              ...
            </li>
          ))}
        </ul>
      </aside>
      */}
    </div>
  );
};

const BattlePlayPage = () => {
  return <BattleContent />;
};

export default BattlePlayPage;
