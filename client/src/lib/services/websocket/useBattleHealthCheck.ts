"use client";

import { useEffect } from "react";
import { useBattleSocketStore } from "@/store/battleStore";
import { useToastStore } from "@/store/toastStore";
import battleSocketClient from "@/lib/services/websocket/battleWebSocketService";

const HEALTH_CHECK_INTERVAL = 5000;
const TIMEOUT_THRESHOLD = 10000;

export const useBattleHealthCheck = (roomId: number, userId: number) => {
  const lastUpdatedAt = useBattleSocketStore((state) => state.lastUpdatedAt);
  const reset = useBattleSocketStore((state) => state.reset);
  const { showToast } = useToastStore();

  useEffect(() => {
    const interval = setInterval(() => {
      if (!lastUpdatedAt || !roomId || !userId) return;

      const now = Date.now();
      const diff = now - lastUpdatedAt;

      if (diff > TIMEOUT_THRESHOLD) {
        console.warn("⛔ WebSocket 응답 없음 - 재연결 시도");
        showToast("연결이 끊겼습니다. 재시도 중...", "error");

        battleSocketClient.retryConnection(roomId, userId);

        // 실패했을 때를 대비한 soft fallback 타이머 (예: 5초 후 reset)
        setTimeout(() => {
          if (useBattleSocketStore.getState().lastUpdatedAt === lastUpdatedAt) {
            console.warn("❌ 재연결 실패로 상태 초기화");
            reset();
          }
        }, 5000);
      }
    }, HEALTH_CHECK_INTERVAL);

    return () => clearInterval(interval);
  }, [lastUpdatedAt, roomId, userId, reset, showToast]);
};
