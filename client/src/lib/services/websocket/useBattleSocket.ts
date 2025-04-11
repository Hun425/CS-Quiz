"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useBattleSocketStore } from "@/store/battleStore";
import { useProfileStore } from "@/store/profileStore";
import battleWebSocketService from "@/lib/services/websocket/battleWebSocketService";
import {
  BattleWebSocketEvents,
  BattleSocketEventKey,
} from "@/lib/types/battle";
/**
 * ✅ 배틀 WebSocket을 초기화하고 이벤트를 상태에 반영하는 훅
 * @param roomId - 현재 배틀 방 ID
 */
export const useBattleSocket = (roomId: number) => {
  const userId = useProfileStore.getState().userProfile?.id;
  const router = useRouter();

  const {
    setParticipantsPayload,
    setStartPayload,
    setStatus,
    setProgress,
    setNextQuestion,
    setResult,
    setEndPayload,
    reset: resetStore,
  } = useBattleSocketStore();

  useEffect(() => {
    if (!roomId || !userId) return;

    const initSocket = async () => {
      try {
        console.log("🌐 WebSocket 연결 시도 중...");
        await battleWebSocketService.connect(roomId, userId);
        console.log("✅ WebSocket 연결 성공");

        battleWebSocketService.on(
          BattleSocketEventKey.PARTICIPANTS,
          (data: BattleWebSocketEvents[BattleSocketEventKey.PARTICIPANTS]) => {
            console.log("📥 [PARTICIPANTS] 참가자 정보 수신:", data);
            setParticipantsPayload({ ...data });
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.START,
          (data: BattleWebSocketEvents[BattleSocketEventKey.START]) => {
            console.log("🚀 [START] 배틀 시작 수신:", data);
            setStartPayload(data);

            setTimeout(() => {
              router.push(`/battles/${roomId}/play`);
            }, 1000); // 1초 후
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.STATUS,
          (data: BattleWebSocketEvents[BattleSocketEventKey.STATUS]) => {
            console.log("📡 [STATUS] 상태 변경 수신:", data);
            setStatus(data.status);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.PROGRESS,
          (data: BattleWebSocketEvents[BattleSocketEventKey.PROGRESS]) => {
            console.log("📊 [PROGRESS] 진행 상황 수신:", data);
            setProgress(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.NEXT_QUESTION,
          (data: BattleWebSocketEvents[BattleSocketEventKey.NEXT_QUESTION]) => {
            console.log("❓ [NEXT_QUESTION] 다음 문제 수신:", data);
            setNextQuestion(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.RESULT,
          (data: BattleWebSocketEvents[BattleSocketEventKey.RESULT]) => {
            console.log("📝 [RESULT] 정답 결과 수신:", data);
            setResult(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.END,
          (data: BattleWebSocketEvents[BattleSocketEventKey.END]) => {
            console.log("🏁 [END] 배틀 종료 수신:", data);
            setEndPayload(data);
          }
        );
      } catch (error) {
        console.error("❌ WebSocket 초기화 실패:", error);
      }
    };

    initSocket();

    return () => {
      console.log("👋 WebSocket 연결 종료 및 상태 초기화");
      battleWebSocketService.clearEventHandlers();
      battleWebSocketService.disconnect();
      resetStore();
    };
  }, [
    roomId,
    userId,
    router,
    setParticipantsPayload,
    setStartPayload,
    setStatus,
    setProgress,
    setNextQuestion,
    setResult,
    setEndPayload,
    resetStore,
  ]);
};
