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
    updateLastActivity,
  } = useBattleSocketStore();

  useEffect(() => {
    if (!roomId || !userId) return;

    const handleParticipants = (
      data: BattleWebSocketEvents[BattleSocketEventKey.PARTICIPANTS]
    ) => {
      console.log("📥 [PARTICIPANTS] 참가자 정보 수신:", data);
      updateLastActivity();
      setParticipantsPayload({ ...data });
    };

    const handleReady = (
      data: BattleWebSocketEvents[BattleSocketEventKey.READY]
    ) => {
      console.log("✅ [READY] 준비 상태 수신:", data);
      updateLastActivity();

      // 1) 현재 저장된 payload 가져오기
      const prev = useBattleSocketStore.getState().participantsPayload;

      // 2) participants 배열만 data.participants로 교체
      setParticipantsPayload({
        ...prev,
        roomId: data.roomId,
        participants: data.participants,
      });
    };

    const handleStart = (
      data: BattleWebSocketEvents[BattleSocketEventKey.START]
    ) => {
      console.log("🚀 [START] 배틀 시작 수신:", data);
      updateLastActivity();
      setStartPayload(data);
      setTimeout(() => {
        router.push(`/battles/${roomId}/play`);
      }, 1000);
    };

    const handleStatus = (
      data: BattleWebSocketEvents[BattleSocketEventKey.STATUS]
    ) => {
      console.log("📡 [STATUS] 상태 변경 수신:", data);
      updateLastActivity();
      setStatus(data.status);
    };

    const handleProgress = (
      data: BattleWebSocketEvents[BattleSocketEventKey.PROGRESS]
    ) => {
      console.log("📊 [PROGRESS] 진행 상황 수신:", data);
      updateLastActivity();
      setProgress(data);
    };

    const handleNext = (
      data: BattleWebSocketEvents[BattleSocketEventKey.NEXT_QUESTION]
    ) => {
      console.log("❓ [NEXT_QUESTION] 다음 문제 수신:", data);
      updateLastActivity();
      setNextQuestion(data);
    };

    const handleResult = (
      data: BattleWebSocketEvents[BattleSocketEventKey.RESULT]
    ) => {
      console.log("📝 [RESULT] 정답 결과 수신:", data);
      updateLastActivity();
      setResult(data);
    };

    const handleEnd = (
      data: BattleWebSocketEvents[BattleSocketEventKey.END]
    ) => {
      console.log("🏁 [END] 배틀 종료 수신:", data);
      updateLastActivity();
      setEndPayload(data);
    };

    const handleError = (
      data: BattleWebSocketEvents[BattleSocketEventKey.ERROR]
    ) => {
      console.error("❌ [ERROR] 에러 수신:", data);
      updateLastActivity();
      alert(data);
    };

    const initSocket = async () => {
      try {
        console.log("🌐 WebSocket 연결 시도 중...");
        await battleWebSocketService.connect(roomId, userId);
        console.log("✅ WebSocket 연결 성공");

        battleWebSocketService.on(
          BattleSocketEventKey.PARTICIPANTS,
          handleParticipants
        );
        battleWebSocketService.on(BattleSocketEventKey.READY, handleReady);
        battleWebSocketService.on(BattleSocketEventKey.START, handleStart);
        battleWebSocketService.on(BattleSocketEventKey.STATUS, handleStatus);
        battleWebSocketService.on(
          BattleSocketEventKey.PROGRESS,
          handleProgress
        );
        battleWebSocketService.on(
          BattleSocketEventKey.NEXT_QUESTION,
          handleNext
        );
        battleWebSocketService.on(BattleSocketEventKey.RESULT, handleResult);
        battleWebSocketService.on(BattleSocketEventKey.END, handleEnd);
        battleWebSocketService.on(BattleSocketEventKey.ERROR, handleError);
      } catch (error) {
        console.error("❌ WebSocket 초기화 실패:", error);
      }
    };

    initSocket();

    return () => {
      // console.log("👋 WebSocket 연결 종료 및 핸들러 해제");
      // battleWebSocketService.off(BattleSocketEventKey.PARTICIPANTS);
      // battleWebSocketService.off(BattleSocketEventKey.START);
      // battleWebSocketService.off(BattleSocketEventKey.STATUS);
      // battleWebSocketService.off(BattleSocketEventKey.PROGRESS);
      // battleWebSocketService.off(BattleSocketEventKey.NEXT_QUESTION);
      // battleWebSocketService.off(BattleSocketEventKey.RESULT);
      // battleWebSocketService.off(BattleSocketEventKey.END);
      // battleWebSocketService.off(BattleSocketEventKey.ERROR);
      // battleWebSocketService.disconnect();
      // resetStore();
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
    updateLastActivity,
  ]);
};
