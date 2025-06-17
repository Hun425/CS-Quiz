"use client";

import { useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { useBattleSocketStore } from "@/store/battleStore";
import { useBattleSocket } from "@/lib/services/websocket/useBattleSocket";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";
import { BattleStatus } from "@/lib/types/battle";
import Loading from "@/app/_components/Loading";
import BattleProgressBoard from "../../_components/BattleProgressBoard";
import BattleQuestion from "../../_components/BattleQuestion";

const BattleContent = () => {
  const { roomid } = useParams();
  const router = useRouter();
  const roomId = Number(roomid);

  useBattleSocket(roomId);

  // store 상태
  const { status, endPayload } = useBattleSocketStore();

  // 로딩 상태: 아직 대기 또는 진행 중이 아닌 경우
  const isLoading =
    status !== BattleStatus.READY && status !== BattleStatus.IN_PROGRESS;

  useEffect(() => {
    if (status === BattleStatus.FINISHED && endPayload) {
      router.replace(`/battles/${roomId}/result`);
    }
  }, [status, endPayload, roomId, router]);

  const handleLeave = () => {
    const ok = window.confirm("정말 배틀을 나가시겠습니까?");
    if (ok) {
      battleSocketClient.leaveBattle();
      useBattleSocketStore.getState().reset();
      router.replace("/battles");
    }
  };

  if (isLoading) return <Loading />;

  return (
    <>
      {/* PC용: 상단 우측 나가기 버튼 */}

      {/* 본문 */}
      <div className="max-w-screen-xl mx-auto px-4 py-6 min-h-screen grid grid-cols-1 sm:grid-cols-[2fr_1fr] gap-6">
        {/* 문제 영역 */}
        <BattleQuestion />

        {/* 사이드: 진행 현황 */}
        <section aria-label="퀴즈 진행 현황" className="space-y-4">
          <BattleProgressBoard />
          <div className="hidden sm:flex justify-end max-w-screen-xl mx-auto px-4 mt-4">
            <button
              onClick={handleLeave}
              className="px-3 py-1.5 text-sm font-medium text-white bg-red-500 rounded-md hover:bg-red-600 transition"
            >
              배틀 나가기
            </button>
          </div>
        </section>
      </div>

      {/* 모바일 전용: 하단 중앙 고정 나가기 버튼 */}
      <div className="sm:hidden mx-auto w-[90%] max-w-sm bg-red-500 hover:bg-red-200 border border-red-500 shadow-md rounded-xl mb-4">
        <button
          onClick={handleLeave}
          className="w-full py-3 text-sm font-semibold text-white"
        >
          배틀 나가기
        </button>
      </div>
    </>
  );
};

const BattlePlayPage = () => {
  return <BattleContent />;
};

export default BattlePlayPage;
