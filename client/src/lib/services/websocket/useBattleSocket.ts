"use client";

import { useEffect } from "react";
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
        await battleWebSocketService.connect(roomId, userId);

        battleWebSocketService.on(
          BattleSocketEventKey.PARTICIPANTS,
          (data: BattleWebSocketEvents["PARTICIPANTS"]) => {
            setParticipantsPayload(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.START,
          (data: BattleWebSocketEvents["START"]) => {
            setStartPayload(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.STATUS,
          (data: BattleWebSocketEvents["STATUS"]) => {
            setStatus(data.status);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.PROGRESS,
          (data: BattleWebSocketEvents["PROGRESS"]) => {
            setProgress(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.NEXT_QUESTION,
          (data: BattleWebSocketEvents["NEXT_QUESTION"]) => {
            setNextQuestion(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.RESULT,
          (data: BattleWebSocketEvents["RESULT"]) => {
            setResult(data);
          }
        );

        battleWebSocketService.on(
          BattleSocketEventKey.END,
          (data: BattleWebSocketEvents["END"]) => {
            setEndPayload(data);
          }
        );
      } catch (error) {
        console.error("❌ WebSocket 초기화 실패:", error);
      }
    };

    initSocket();

    return () => {
      battleWebSocketService.clearEventHandlers();
      battleWebSocketService.disconnect();
      resetStore();
    };
  }, [
    roomId,
    userId,
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
